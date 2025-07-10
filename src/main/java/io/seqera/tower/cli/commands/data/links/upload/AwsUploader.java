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

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DataLinksApi;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.cli.utils.progress.ProgressTrackingBodyPublisher;
import io.seqera.tower.model.DataLinkFinishMultiPartUploadRequest;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;
import io.seqera.tower.model.UploadEtag;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AwsUploader extends AbstractProviderUploader {

    private final String id;
    private final String credId;
    private final Long wspId;
    private final String outputDir;
    private final String relativeKey;
    private final DataLinksApi dataLinksApi;

    public AwsUploader(String id, String credId, Long wspId, String outputDir, String relativeKey, DataLinksApi dataLinksApi) {
        this.id = id;
        this.credId = credId;
        this.wspId = wspId;
        this.outputDir = outputDir;
        this.relativeKey = relativeKey;
        this.dataLinksApi = dataLinksApi;
    }

    @Override
    public void uploadFile(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) throws ApiException {
        int index = 0;
        boolean withError = false;
        List<UploadEtag> tags = new ArrayList<>();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String url : urlResponse.getUploadUrls()) {
                byte[] chunk = getChunk(file, index);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .PUT(new ProgressTrackingBodyPublisher(chunk, tracker))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    withError = true;
                    throw new IOException("Failed to upload file: HTTP " + response.statusCode() +"Message: " + response.body());
                }

                Optional<String> etag = response.headers().firstValue("ETag");

                if (etag.isPresent()) {
                    UploadEtag uploadEtag = new UploadEtag();
                    uploadEtag.eTag(etag.get());
                    uploadEtag.partNumber(index+1);
                    tags.add(uploadEtag);
                }
                else {
                    throw new TowerRuntimeException("Failed to upload file: Possible CORS issue");
                }
                index++;
            }
        } catch (Exception e) {
            withError = true;
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        } finally {
            finalizeUpload(urlResponse, withError, tags);
        }
    }

    private void finalizeUpload(DataLinkMultiPartUploadResponse urlResponse, boolean withError, List<UploadEtag> tags) throws ApiException {
        // Finalize the upload
        DataLinkFinishMultiPartUploadRequest finishMultiPartUploadRequest = new DataLinkFinishMultiPartUploadRequest();
        finishMultiPartUploadRequest.setFileName(relativeKey);
        finishMultiPartUploadRequest.setUploadId(urlResponse.getUploadId());
        finishMultiPartUploadRequest.setWithError(withError);
        finishMultiPartUploadRequest.setTags(tags);

        if (outputDir != null) {
            dataLinksApi.finishDataLinkUpload1(id, outputDir, finishMultiPartUploadRequest, credId, wspId);
        } else {
            dataLinksApi.finishDataLinkUpload(id, finishMultiPartUploadRequest, credId, wspId);
        }
    }

    @Override
    public void abortUpload(DataLinkMultiPartUploadResponse urlResponse) throws ApiException {
        finalizeUpload(urlResponse,  true, Collections.emptyList());
    }
}