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
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinkContentList;
import io.seqera.tower.model.DataLinkContentResponse;
import io.seqera.tower.model.DataLinkDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "browse",
        description = "Browse content of data-link."
)
public class BrowseCmd extends AbstractDataLinksCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataLinkRefOptions dataLinkRefOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier.")
    public String credentialsRef;

    @CommandLine.Option(names = {"-p", "--path"}, description = "Path to the folder to browse.")
    public String path;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Filter files with the given prefix.")
    public String startsWith;

    @CommandLine.Option(names = {"-t", "--token"}, description = "Next page token to fetch next page.")
    public String nextPageToken;

    @CommandLine.Option(names = {"--page"}, description = "Pages to display [default: null].")
    public Integer page;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        String credId = credentialsRef != null ? credentialsByRef(null, wspId, credentialsRef) : null;
        String id = getDataLinkId(dataLinkRefOptions, wspId, credId);

        DataLinkDto dataLink = dataLinksApi().describeDataLink(id, wspId, credId).getDataLink();
        DataLinkContentResponse response;

        if (path != null)
            response = dataLinksApi().exploreDataLinkWithPath(id, path, wspId, credId, startsWith, nextPageToken, page);
        else {
            response = dataLinksApi().exploreDataLink(id, wspId, credId, startsWith, nextPageToken, page);
        }

        return new DataLinkContentList(dataLink, path, response.getObjects(), response.getNextPageToken());
    }

}
