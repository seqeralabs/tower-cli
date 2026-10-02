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

package io.seqera.tower.cli.commands.data.links;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkDownloadScript;
import io.seqera.tower.model.DataLinkDownloadScriptResponse;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = "download-script",
        description = "Print the cloud provider CLI commands that download data link contents. Downloads the whole data link when no --file or --dir is given."
)
public class DownloadScriptCmd extends AbstractDataLinksCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataLinkRefOptions dataLinkRefOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier")
    public String credentialsRef;

    @CommandLine.Option(names = {"--file"}, description = "File path to download, relative to the data link root. Can be specified multiple times.")
    public List<String> files;

    @CommandLine.Option(names = {"--dir"}, description = "Folder path to download, relative to the data link root. Can be specified multiple times.")
    public List<String> dirs;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String credId = credentialsRef != null ? credentialsByRef(null, wspId, credentialsRef) : null;
        String id = getDataLinkId(dataLinkRefOptions, wspId, credId);

        DataLinkDownloadScriptResponse response = dataLinksApi().generateDownloadScript(id, wspId, credId, toObjects(dirs), toObjects(files));

        return new DataLinkDownloadScript(response.getScript());
    }

    private static List<Object> toObjects(List<String> paths) {
        return paths == null ? null : new ArrayList<>(paths);
    }
}
