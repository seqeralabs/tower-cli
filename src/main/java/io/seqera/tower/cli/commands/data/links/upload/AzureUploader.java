/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.commands.data.links.upload;

import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.cli.utils.progress.ProgressTrackingBodyPublisher;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public class AzureUploader extends AbstractProviderUploader {

    @Override
    public void uploadFile(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) {
        List<String> urls = urlResponse.getUploadUrls();

        HttpClient client = HttpClient.newHttpClient();
        try {
            // Upload chunks
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                byte[] chunk = getChunk(file, i);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .PUT(new ProgressTrackingBodyPublisher(chunk, tracker))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 201) {
                    // Abort the upload before throwing the exception
                    throw new IOException("Failed to upload chunk: HTTP " + response.statusCode());
                }
            }

            // Finalize the upload by sending list of block IDs
            finalizeUpload(urls, client);

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