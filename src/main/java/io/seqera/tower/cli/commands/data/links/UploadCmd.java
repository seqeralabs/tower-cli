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
import java.util.stream.Collectors;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkFileTransferResult;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.cli.utils.progress.ProgressTrackingBodyPublisher;
import io.seqera.tower.model.DataLinkDto;
import io.seqera.tower.model.DataLinkFinishMultiPartUploadRequest;
import io.seqera.tower.model.DataLinkItemType;
import io.seqera.tower.model.DataLinkMultiPartUploadRequest;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;
import io.seqera.tower.model.DataLinkProvider;
import io.seqera.tower.model.UploadEtag;
import picocli.CommandLine;

@CommandLine.Command(
        name = "upload",
        description = "Upload content to data link."
)
public class UploadCmd extends AbstractDataLinksCmd {

    static final Integer MULTI_UPLOAD_PART_SIZE_IN_BYTES = 250 * 1024 * 1024; // 250 MB - synced w
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

    @CommandLine.Parameters(arity = "1..*", description = "Paths to files or directories to upload")
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

    private void uploadFile(File file, String relativeKey, String id, String credId, Long wspId, DataLinkProvider provider) throws ApiException, IOException, InterruptedException {
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

        switch (provider) {
            case AWS:
                uploadFileToAws(file, relativeKey, id, credId, wspId, urlResponse, tracker);
                break;
            case GOOGLE:
                uploadFileToGoogle(file, urlResponse, tracker);
                break;
            case AZURE:
                uploadFileToAzure(file, urlResponse, tracker);
                break;
            default:
                throw new TowerRuntimeException("Unsupported data-link provider: " + provider);
        }
    }

    private void uploadFileToAws(File file, String relativeKey, String id, String credId, Long wspId, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) throws ApiException {
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
                    throw new IOException("Failed to upload file: HTTP " + response.statusCode());
                }

                Optional<String> etag = response.headers().firstValue("ETag");

                if (etag.isPresent()) {
                    UploadEtag uploadEtag = new UploadEtag();
                    uploadEtag.eTag(etag.get());
                    uploadEtag.partNumber(index+1);
                    tags.add(uploadEtag);
                }
                else {
                    throw new TowerRuntimeException("Possible CORS issue");
                }
                index++;
            }
        } catch (Exception e) {
            withError = true;
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        } finally {
            // Finalize the upload
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

    private void uploadFileToGoogle(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) throws ApiException {
        String url = urlResponse.getUploadUrls().get(0);
        long fileSize = file.length();
        long nextByteToRead = 0;

        try (HttpClient client = HttpClient.newHttpClient()) {
            while (nextByteToRead < fileSize) {
                long end = Math.min(nextByteToRead + MULTI_UPLOAD_PART_SIZE_IN_BYTES, fileSize);
                byte[] chunk = getChunk(file, (int)(nextByteToRead / MULTI_UPLOAD_PART_SIZE_IN_BYTES));

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
                    // Cancel the upload by sending a DELETE request
                    HttpRequest deleteRequest = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .DELETE()
                            .build();
                    client.send(deleteRequest, HttpResponse.BodyHandlers.discarding());
                    throw new IOException("Failed to upload file: HTTP " + response.statusCode());
                } else {
                    break; // Upload completed successfully
                }
            }
        } catch (Exception e) {
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    private void uploadFileToAzure(File file, DataLinkMultiPartUploadResponse urlResponse, ProgressTracker tracker) throws ApiException {
        List<String> urls = urlResponse.getUploadUrls();

        try (HttpClient client = HttpClient.newHttpClient()) {
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
                    throw new IOException("Failed to upload chunk: HTTP " + response.statusCode());
                }
            }

            // Finalize the upload by sending list of block IDs
            finalizeAzureUpload(urls, client);

        } catch (Exception e) {
            throw new TowerRuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Finalize the Azure upload sending the list of block IDs to Azure, so it can merge all the chunks received in a single file.
     *
     * @param urls The list of urls generated for the Azure upload, required to obtain the list of block IDs
     */
    private void finalizeAzureUpload(List<String> urls, HttpClient client) throws IOException, InterruptedException {
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

    private byte[] getChunk(File file, int index) {
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

    private void checkFilesValidForUpload() {
        // Check total number of files across all paths
        int totalFiles = 0;
        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                totalFiles += countFilesInDirectoryAndCheckFileSize(file);
            } else {
                totalFiles++;
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
