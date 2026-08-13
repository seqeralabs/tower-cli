/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.tower.cli.commands.data.links.upload;

import io.seqera.tower.api.DataLinksApi;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AzureUploader extends AbstractProviderUploader {

    public AzureUploader(String id, String credId, Long wspId, String outputDir, String relativeKey, DataLinksApi dataLinksApi, int concurrency) {
        super(id, credId, wspId, outputDir, relativeKey, dataLinksApi, concurrency);
    }

    @Override
    public void uploadFile(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) {
        long contentLength = file.length();

        List<String> initialUrls = urlResponse.getUploadUrls();
        Map<Integer, String> partUrls = new ConcurrentHashMap<>();
        for (int i = 0; i < initialUrls.size(); i++) {
            partUrls.put(i + 1, initialUrls.get(i));
        }
        int totalParts = initialUrls.size();

        try (HttpClient client = HttpClient.newHttpClient()) {
            checkPartCount(contentLength, totalParts);

            uploadPartsInParallel(totalParts, partNumber -> {
                byte[] chunk = getChunk(file, partNumber - 1);
                uploadPartWithRetry(client, partUrls, partNumber, chunk, tracker, null, contentLength, 201, false);
                return Boolean.TRUE; // result unused; must be non-null to mark the part done
            });

            // Finalize the upload by sending the ordered list of block IDs
            List<String> orderedUrls = new ArrayList<>();
            for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
                orderedUrls.add(partUrls.get(partNumber));
            }
            finalizeUpload(orderedUrls, client);

        } catch (Exception e) {
            abortUpload(urlResponse);
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void abortUpload(DataLinkMultiPartUploadResponse urlResponse) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String initialUrl = getFinalizeUrl(urlResponse.getUploadUrls().get(0));
            String abortUrl = getFinalizeUrl(initialUrl);
            // Send an empty block list to abort the upload
            // Per Azure documentation, any Uncommitted blocks not part of the final BlockList are garbage collected
            String emptyBlockList = "<?xml version=\"1.0\" encoding=\"utf-8\"?><BlockList></BlockList>";

            HttpRequest abortRequest = HttpRequest.newBuilder()
                    .uri(URI.create(abortUrl))
                    .PUT(HttpRequest.BodyPublishers.ofString(emptyBlockList))
                    .build();

            client.send(abortRequest, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new TowerRuntimeException("Failed to upload file and encountered error while attempting to cancel upload " + e.getMessage(), e);
        }
    }

    private void finalizeUpload(List<String> urls, HttpClient client) throws IOException, InterruptedException {
        String finalizeUrl = getFinalizeUrl(urls.get(0));
        List<String> blockIds = urls.stream()
                .map(this::extractBlockId)
                .collect(Collectors.toList());

        String blockList = buildBlockList(blockIds);

        HttpRequest finalizeRequest = HttpRequest.newBuilder()
                .uri(URI.create(finalizeUrl))
                .PUT(HttpRequest.BodyPublishers.ofString(blockList))
                .build();

        HttpResponse<String> response = client.send(finalizeRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201) {
            throw new IOException("Failed to finalize Azure upload: HTTP " + response.statusCode());
        }
    }

    private String extractBlockId(String url) {
        String blockIdSubstring = "blockid=";
        int start = url.indexOf(blockIdSubstring) + blockIdSubstring.length();
        int end = url.indexOf('&', start);
        return end > start ? url.substring(start, end) : url.substring(start);
    }

    private String getFinalizeUrl(String initialUrl) {
        return initialUrl.replaceAll("(blockid=[^&]*&)", "")
                        .replaceAll("(comp=[^&]*)", "comp=blocklist");
    }

    private String buildBlockList(List<String> blockIds) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?><BlockList>");
        for (String id : blockIds) {
            xml.append("<Uncommitted>").append(id).append("</Uncommitted>");
        }
        xml.append("</BlockList>");
        return xml.toString();
    }
}
