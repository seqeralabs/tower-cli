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
import io.seqera.tower.cli.utils.progress.PartProgress;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.cli.utils.progress.ProgressTrackingBodyPublisher;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GoogleUploader extends AbstractProviderUploader {

    public GoogleUploader(String id, String credId, Long wspId, String outputDir, String relativeKey, DataLinksApi dataLinksApi, int concurrency) {
        super(id, credId, wspId, outputDir, relativeKey, dataLinksApi, concurrency);
    }

    @Override
    public void uploadFile(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) {
        String url = urlResponse.getUploadUrls().get(0);
        long fileSize = file.length();
        long nextByteToRead = 0;

        HttpClient client = HttpClient.newHttpClient();
        try {
            while (nextByteToRead < fileSize) {
                int partNumber = (int) (nextByteToRead / partSizeBytes());
                byte[] chunk = getChunk(file, partNumber);
                final long start = nextByteToRead;
                final long end = start + chunk.length;
                PartProgress part = tracker.newPart();

                HttpResponse<String> response = sendWithRetryOnTransientError(client, part,
                        () -> HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .PUT(new ProgressTrackingBodyPublisher(chunk, part))
                                .header("Content-Range", String.format("bytes %d-%d/%d", start, Math.max(0, end - 1), fileSize))
                                .build());

                if (response.statusCode() == 308) {
                    // Resume upload from the last byte received by the server
                    String range = response.headers().firstValue("range").orElse("");
                    if (!range.isEmpty()) {
                        long lastByte = Long.parseLong(range.substring(range.lastIndexOf('-') + 1));
                        nextByteToRead = lastByte + 1;
                    }
                } else if (response.statusCode() == 200) {
                    break; // Upload completed successfully
                } else {
                    part.reset();
                    throw new IOException("Failed to upload file: HTTP " + response.statusCode());
                }
            }
        } catch (Exception e) {
            abortUpload(urlResponse);
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void abortUpload(DataLinkMultiPartUploadResponse urlResponse) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = urlResponse.getUploadUrls().get(0);

            // Cancel the upload by sending a DELETE request
            HttpRequest deleteRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();

            client.send(deleteRequest, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            throw new TowerRuntimeException("Failed to upload file and encountered error while attempting to cancel upload " + e.getMessage(), e);
        }
    }
}
