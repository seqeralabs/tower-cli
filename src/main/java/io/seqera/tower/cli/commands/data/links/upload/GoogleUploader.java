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

import io.seqera.tower.ApiException;
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

public class GoogleUploader extends AbstractProviderUploader {

    @Override
    public void uploadFile(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) {
        String url = urlResponse.getUploadUrls().get(0);
        long fileSize = file.length();
        long nextByteToRead = 0;

        HttpClient client = HttpClient.newHttpClient();
        try {
            while (nextByteToRead < fileSize) {
                int partNumber = (int)(nextByteToRead / MULTI_UPLOAD_PART_SIZE_IN_BYTES);
                byte[] chunk = getChunk(file, partNumber);
                long end = nextByteToRead + chunk.length;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .PUT(new ProgressTrackingBodyPublisher(chunk, tracker))
                        .header("Content-Range", String.format("bytes %d-%d/%d", nextByteToRead, Math.max(0, end - 1), fileSize))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 308) {
                    // Resume upload from the last byte received by the server
                    String range = response.headers().firstValue("range").orElse("");
                    if (!range.isEmpty()) {
                        long lastByte = Long.parseLong(range.substring(range.lastIndexOf('-') + 1));
                        nextByteToRead = lastByte + 1;
                    }
                } else if (response.statusCode() != 200) {
                    throw new IOException("Failed to upload file: HTTP " + response.statusCode());
                } else {
                    break; // Upload completed successfully
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