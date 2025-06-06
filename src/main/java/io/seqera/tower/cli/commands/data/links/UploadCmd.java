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

package io.seqera.tower.cli.commands.data.links;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.data.links.upload.CloudProviderUploader;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkFileTransferResult;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.model.DataLinkDto;
import io.seqera.tower.model.DataLinkItemType;
import io.seqera.tower.model.DataLinkMultiPartUploadRequest;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;
import io.seqera.tower.model.DataLinkProvider;
import io.seqera.tower.cli.commands.data.links.upload.AwsUploader;
import io.seqera.tower.cli.commands.data.links.upload.GoogleUploader;
import io.seqera.tower.cli.commands.data.links.upload.AzureUploader;
import picocli.CommandLine;

@CommandLine.Command(
        name = "upload",
        description = "Upload content to data-link."
)
public class UploadCmd extends AbstractDataLinksCmd {

    static final long MAX_FILE_SIZE = 5L * 1024L * 1024L * 1024L * 1024L; // 5 TB

    static final Integer MAX_FILES_TO_UPLOAD = 300;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataLinkRefOptions dataLinkRefOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier.", required = true)
    public String credentialsRef;

    @CommandLine.Option(names = {"-o", "--output-dir"}, description = "Output directory.")
    public String outputDir;

    @CommandLine.Parameters(arity = "1..*", description = "Paths to files or directories to upload.")
    private List<String> paths;

    @Override
    protected Response exec() throws ApiException, IOException, InterruptedException {
        checkFilesValidForUpload();

        Long wspId = workspaceId(workspace.workspace);
        String credId = credentialsRef != null ? credentialsByRef(null, wspId, credentialsRef) : null;

        DataLinkDto dataLink = getDataLink(dataLinkRefOptions, wspId, credId);
        String id = dataLink.getId();
        DataLinkProvider provider = dataLink.getProvider();

        List<DataLinkFileTransferResult.SimplePathInfo> pathInfo = new ArrayList<>();

        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                String basePrefix = file.getName() + "/";
                int fileCount = uploadDirectory(file, file, basePrefix, id, credId, wspId, provider);
                pathInfo.add(new DataLinkFileTransferResult.SimplePathInfo(DataLinkItemType.FOLDER, path, fileCount));
            } else {
                uploadFile(file, file.getName(), id, credId, wspId, provider);
                pathInfo.add(new DataLinkFileTransferResult.SimplePathInfo(DataLinkItemType.FILE, path, 1));
            }
        }

        return DataLinkFileTransferResult.uploaded(pathInfo);
    }

    private int uploadDirectory(File baseDir, File currentDir, String basePrefix, String id, String credId, Long wspId, DataLinkProvider provider) throws ApiException, IOException, InterruptedException {
        File[] files = currentDir.listFiles();
        if (files == null) return 0;

        int totalFiles = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                totalFiles += uploadDirectory(baseDir, file, basePrefix, id, credId, wspId, provider);
            } else {
                String relativePath = baseDir.toPath().relativize(file.toPath()).toString();
                String fullKey = basePrefix + relativePath;
                uploadFile(file, fullKey, id, credId, wspId, provider);
                totalFiles++;
            }
        }
        return totalFiles;
    }

    private void uploadFile(File file, String relativeKey, String id, String credId, Long wspId, DataLinkProvider provider) throws ApiException, IOException {
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getPath());
        }

        String mimeType = Files.probeContentType(file.toPath()); // Detect MIME type
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        long contentLength = file.length();

        boolean showProgress = app().output != OutputType.json;
        if (showProgress) {
            app().getOut().println("Uploading file: " + file.getPath());
        }

        // Get upload URL
        DataLinkMultiPartUploadRequest uploadRequest = new DataLinkMultiPartUploadRequest();
        uploadRequest.setFileName(relativeKey);
        uploadRequest.setContentLength(contentLength);
        uploadRequest.setContentType(mimeType);

        DataLinkMultiPartUploadResponse urlResponse;
        if (outputDir != null) {
            urlResponse = dataLinksApi().generateDataLinkUploadUrl1(id, outputDir, uploadRequest, credId, wspId, null);
        } else {
            urlResponse = dataLinksApi().generateDataLinkUploadUrl(id, uploadRequest, credId, wspId, null);
        }

        ProgressTracker tracker = new ProgressTracker(app().getOut(), showProgress, contentLength);

        CloudProviderUploader uploader = createUploadStrategy(provider, id, credId, wspId, outputDir, relativeKey);
        uploader.uploadFile(file, urlResponse, tracker);
    }

    private CloudProviderUploader createUploadStrategy(DataLinkProvider provider, String id, String credId, Long wspId, String outputDir, String relativeKey) throws ApiException {
        switch (provider) {
            case AWS:
                return new AwsUploader(id, credId, wspId, outputDir, relativeKey, dataLinksApi());
            case GOOGLE:
                return new GoogleUploader();
            case AZURE:
                return new AzureUploader();
            default:
                throw new TowerRuntimeException("Unsupported data-link provider: " + provider);
        }
    }

    private void checkFilesValidForUpload() {
        // Check total number of files across all paths
        int totalFiles = 0;
        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                totalFiles += countFilesInDirectoryAndCheckFileSize(file);
            } else {
                totalFiles++;
                if (file.length() > MAX_FILE_SIZE) {
                    throw new TowerRuntimeException("File " + file.getPath() + " exceeds maximum size of 5 TB to upload.");
                }
            }

            if (totalFiles > MAX_FILES_TO_UPLOAD) {
                throw new TowerRuntimeException("Cannot upload more than " + MAX_FILES_TO_UPLOAD + " files at once. " +
                        "Found at least " + totalFiles + " files at provided paths. Please reduce number of files to upload in single batch to " + MAX_FILES_TO_UPLOAD + ".");
            }
        }
    }

    private int countFilesInDirectoryAndCheckFileSize(File directory) {
        File[] files = directory.listFiles();
        if (files == null) return 0;

        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += countFilesInDirectoryAndCheckFileSize(file);
            } else {
                count++;
                if (file.length() > MAX_FILE_SIZE) {
                    throw new TowerRuntimeException("File " + file.getPath() + " exceeds maximum size of 5 TB to upload.");
                }
            }
        }
        return count;
    }
}
