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
import io.seqera.tower.cli.responses.data.DataLinkFileDownloadResult;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.DataLinkDownloadUrlResponse;
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
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.printProgressBar;

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

        String id = getDataLinkId(dataLinkRefOptions, wspId);

        for (String path : paths) {
            String filename = FilesHelper.extractFilenameFromPath(path);

            DataLinkDownloadUrlResponse urlResponse = dataLinksApi().generateDownloadUrlDataLink(id, path, credId, wspId, false);

            Path targetPath = outputDir == null ?
                    Paths.get(filename)
                    : Paths.get(outputDir, filename);

            boolean showProgress = app().output != OutputType.json;

            if (showProgress) {
                app().getOut().println("Downloading file: " + path);
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


            try (InputStream in = response.body();
                 OutputStream output = Files.newOutputStream(targetPath)) {

                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                int bytesRead;
                int lastProgress = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    if (contentLength > 0 && showProgress) {
                        int progress = (int) (100 * totalBytesRead / contentLength);
                        if (progress != lastProgress) {
                            printProgressBar(app().getOut(), progress);
                            lastProgress = progress;
                        }
                    }
                }

                if (contentLength > 0 && showProgress) {
                    printProgressBar(app().getOut(), 100);
                    app().getOut().println();
                }
            }
        }

        return new DataLinkFileDownloadResult(paths);
    }

}
