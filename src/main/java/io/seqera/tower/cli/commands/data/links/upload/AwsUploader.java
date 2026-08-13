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
import io.seqera.tower.api.DataLinksApi;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;
import io.seqera.tower.model.UploadEtag;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AwsUploader extends AbstractProviderUploader {

    public AwsUploader(String id, String credId, Long wspId, String outputDir, String relativeKey, DataLinksApi dataLinksApi) {
        super(id, credId, wspId, outputDir, relativeKey, dataLinksApi);
    }

    @Override
    public void uploadFile(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) throws ApiException {
        boolean withError = false;
        List<UploadEtag> tags = new ArrayList<>();
        String uploadId = urlResponse.getUploadId();
        long contentLength = file.length();

        // Seed the part-number -> URL map from the initially generated URLs (positional: index+1 == partNumber).
        // The map is updated in place by uploadPartWithRetry whenever URLs are refreshed on expiry.
        List<String> initialUrls = urlResponse.getUploadUrls();
        Map<Integer, String> partUrls = new HashMap<>();
        for (int i = 0; i < initialUrls.size(); i++) {
            partUrls.put(i + 1, initialUrls.get(i));
        }
        int totalParts = initialUrls.size();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (int partNumber = 1; partNumber <= totalParts; partNumber++) {
                byte[] chunk = getChunk(file, partNumber - 1);

                HttpResponse<String> response = uploadPartWithRetry(client, partUrls, partNumber, chunk, tracker, uploadId, contentLength, 200, true);

                Optional<String> etag = response.headers().firstValue("ETag");
                if (etag.isPresent()) {
                    UploadEtag uploadEtag = new UploadEtag();
                    uploadEtag.eTag(etag.get());
                    uploadEtag.partNumber(partNumber);
                    tags.add(uploadEtag);
                } else {
                    throw new TowerRuntimeException("Failed to upload file: Possible CORS issue");
                }
            }
        } catch (Exception e) {
            withError = true;
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        } finally {
            finishUpload(urlResponse.getUploadId(), withError, tags);
        }
    }

    @Override
    public void abortUpload(DataLinkMultiPartUploadResponse urlResponse) throws ApiException {
        finishUpload(urlResponse.getUploadId(), true, Collections.emptyList());
    }
}
