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

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkFileTransferResult;
import io.seqera.tower.cli.utils.progress.ProgressInputStream;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.model.DataLinkContentTreeListResponse;
import io.seqera.tower.model.DataLinkDownloadUrlResponse;
import io.seqera.tower.model.DataLinkItemType;
import io.seqera.tower.model.DataLinkSimpleItem;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = "download",
        description = "Download content of data link."
)
public class DownloadCmd extends AbstractDataLinksCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataLinkRefOptions dataLinkRefOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier.", required = true)
    public String credentialsRef;

    @CommandLine.Option(names = {"-o", "--output-dir"}, description = "Output directory.")
    public String outputDir;

    @CommandLine.Parameters(arity = "1..*", description = "Paths to files to download")
    private List<String> paths;

    @Override
    protected Response exec() throws ApiException, IOException, InterruptedException {
        Long wspId = workspaceId(workspace.workspace);
        String credId = credentialsRef != null ? credentialsByRef(null, wspId, credentialsRef) : null;

        String id = getDataLinkId(dataLinkRefOptions, wspId, credId);

        List<DataLinkFileTransferResult.SimplePathInfo> pathInfo = new ArrayList<>();
        for (String path : paths) {
            DataLinkContentTreeListResponse browseTreeResponse = dataLinksApi().exploreDataLinkTree(id, wspId, credId, List.of(path));

            if (browseTreeResponse.getItems().isEmpty()) {
                // If the browse-tree response is empty, assume this is a filepath and not a prefix

                String filename = Paths.get(path).getFileName().toString();
                Path targetPath = outputDir == null
                        ? Paths.get(filename)
                        : Paths.get(outputDir, filename);

                downloadFile(path, id, credId, wspId, targetPath);

                pathInfo.add(new DataLinkFileTransferResult.SimplePathInfo(DataLinkItemType.FILE, path, 1));
            }
            else {
                // Download each file for that prefix
                for (DataLinkSimpleItem item : browseTreeResponse.getItems()) {

                    Path targetPath = outputDir == null
                            ? Paths.get(item.getPath())
                            : Paths.get(outputDir, item.getPath());
                    Files.createDirectories(targetPath.getParent());

                    downloadFile(item.getPath(), id, credId, wspId, targetPath);
                }
                pathInfo.add(new DataLinkFileTransferResult.SimplePathInfo(DataLinkItemType.FOLDER, path, browseTreeResponse.getItems().size()));
            }

        }

        return DataLinkFileTransferResult.donwloaded(pathInfo);
    }

    private void downloadFile(String path, String id, String credId, Long wspId, Path targetPath) throws ApiException, IOException, InterruptedException {
        DataLinkDownloadUrlResponse urlResponse = dataLinksApi().generateDownloadUrlDataLink(id, path, credId, wspId, false);

        boolean showProgress = app().output != OutputType.json;

        if (showProgress) {
            app().getOut().println("  Downloading file: " + path);
        }

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlResponse.getUrl()))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to download file: HTTP " + response.statusCode());
        }

        long contentLength = response.headers()
                .firstValueAsLong("Content-Length")
                .orElse(-1);

        ProgressTracker tracker = new ProgressTracker(app().getOut(), showProgress, contentLength);
        try (InputStream in = new ProgressInputStream(response.body(), tracker);
             OutputStream output = Files.newOutputStream(targetPath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

        }
    }
}
