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
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkFileTransferResult;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.cli.utils.progress.ProgressTrackingBodyPublisher;
import io.seqera.tower.model.DataLinkFinishMultiPartUploadRequest;
import io.seqera.tower.model.DataLinkItemType;
import io.seqera.tower.model.DataLinkMultiPartUploadRequest;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;
import io.seqera.tower.model.UploadEtag;
import picocli.CommandLine;

@CommandLine.Command(
        name = "upload",
        description = "Upload content to data link."
)
public class UploadCmd extends AbstractDataLinksCmd {

    static final Integer MULTI_UPLOAD_PART_SIZE_IN_BYTES = 250 * 1024 * 1024; // 250 MB - synced w

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataLinkRefOptions dataLinkRefOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier.", required = true)
    public String credentialsRef;

    @CommandLine.Option(names = {"-o", "--output-dir"}, description = "Output directory.")
    public String outputDir;

    @CommandLine.Parameters(arity = "1..*", description = "Paths to files or directories to upload")
    private List<String> paths;

    @Override
    protected Response exec() throws ApiException, IOException, InterruptedException {
        Long wspId = workspaceId(workspace.workspace);
        String credId = credentialsRef != null ? credentialsByRef(null, wspId, credentialsRef) : null;

        String id = getDataLinkId(dataLinkRefOptions, wspId, credId);

        List<DataLinkFileTransferResult.SimplePathInfo> pathInfo = new ArrayList<>();

        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                String basePrefix = file.getName() + "/";
                int fileCount = uploadDirectory(file, file, basePrefix, id, credId, wspId);
                pathInfo.add(new DataLinkFileTransferResult.SimplePathInfo(DataLinkItemType.FOLDER, path, fileCount));
            } else {
                uploadFile(file, file.getName(), id, credId, wspId);
                pathInfo.add(new DataLinkFileTransferResult.SimplePathInfo(DataLinkItemType.FILE, path, 1));
            }
        }

        return DataLinkFileTransferResult.uploaded(pathInfo);
    }

    private int uploadDirectory(File baseDir, File currentDir, String basePrefix, String id, String credId, Long wspId) throws ApiException, IOException, InterruptedException {
        File[] files = currentDir.listFiles();
        if (files == null) return 0;

        int totalFiles = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                totalFiles += uploadDirectory(baseDir, file, basePrefix, id, credId, wspId);
            } else {
                String relativePath = baseDir.toPath().relativize(file.toPath()).toString();
                String fullKey = basePrefix + relativePath;
                uploadFile(file, fullKey, id, credId, wspId);
                totalFiles++;
            }
        }
        return totalFiles;
    }

    private void uploadFile(File file, String relativeKey, String id, String credId, Long wspId) throws ApiException, IOException, InterruptedException {
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
        // If output directory is specified, prepend it to the relative key
//        String finalKey = outputDir != null ? outputDir + "/" + relativeKey : relativeKey;
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

        int index = 0;
        boolean withError = false;
        List<UploadEtag> tags = new ArrayList<>();
        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String url : urlResponse.getUploadUrls()) {

                byte[] chunk = getFileChunk(file, index);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .PUT(new ProgressTrackingBodyPublisher(chunk, tracker))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    withError = true;
                    throw new IOException("Failed to upload file: HTTP " + response.statusCode());
                }

                Optional<String> etag = response.headers().firstValue("ETag");

                if (etag.isPresent()) {
                    UploadEtag uploadEtag = new UploadEtag();
                    uploadEtag.eTag(etag.get());
                    uploadEtag.partNumber(index+1);
                    tags.add(uploadEtag);
                }
                else   {
                    throw new TowerRuntimeException("Failed to upload file: Possible CORS issue");
                }
                index++;
            }

        } catch (Exception e) {
            withError = true;
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        } finally {
            // Step 3: Finalize the upload
            DataLinkFinishMultiPartUploadRequest finishMultiPartUploadRequest = new DataLinkFinishMultiPartUploadRequest();
            finishMultiPartUploadRequest.setFileName(relativeKey);
            finishMultiPartUploadRequest.setUploadId(urlResponse.getUploadId());
            finishMultiPartUploadRequest.setWithError(withError);
            finishMultiPartUploadRequest.setTags(tags);

            if (outputDir != null) {
                dataLinksApi().finishDataLinkUpload1(id, outputDir, finishMultiPartUploadRequest, credId, wspId);
            } else {
                dataLinksApi().finishDataLinkUpload(id, finishMultiPartUploadRequest, credId, wspId);

            }
        }
    }

    private byte[] getFileChunk(File file, int index) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long start = (long) index * MULTI_UPLOAD_PART_SIZE_IN_BYTES;
            long end = Math.min(start + MULTI_UPLOAD_PART_SIZE_IN_BYTES, file.length());
            int length = (int) (end - start);

            byte[] buffer = new byte[length];
            raf.seek(start);
            raf.readFully(buffer);

            return buffer;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
