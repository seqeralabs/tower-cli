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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinksList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.DataLinksListResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        description = "List data links."
)
public class ListCmd extends AbstractDataLinksCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Show only data links that can be ")
    public String credentialsRef;

    // Search params
    @CommandLine.Mixin
    SearchOption searchOption;


    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        Long wspId = workspaceId(workspace.workspace);

        String search = buildSearch(searchOption.startsWith, searchOption.providers, searchOption.region, searchOption.uri);

        DataLinksListResponse response = api().listDataLinks(
                wspId, credentialsRef, search, max, offset, null
        );

        return new DataLinksList(workspaceRef(wspId), response.getDataLinks(), PaginationInfo.from(offset, max, response.getTotalSize()));
    }
}
