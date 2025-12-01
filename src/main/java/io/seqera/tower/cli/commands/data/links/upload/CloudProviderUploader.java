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
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;

import java.io.File;

public interface CloudProviderUploader {
    /**
     * Upload a file using the provider-specific strategy
     *
     * @param file The file to upload
     * @param urlResponse The upload URLs and metadata from Platform
     * @param tracker Progress tracker for upload status
     * @throws ApiException If there's an error communicating with the API
     */
    void uploadFile(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) throws ApiException;

    /**
     * Abort upload of a file using the provider-specific strategy
     *
     * @param urlResponse The upload URLs and metadata from Platform
     * @throws ApiException If there's an error communicating with the API
     */
    void abortUpload(DataLinkMultiPartUploadResponse urlResponse) throws ApiException;
}
