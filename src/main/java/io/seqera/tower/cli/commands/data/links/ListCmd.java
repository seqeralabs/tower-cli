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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinksList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.data.DataLinkProvider;
import io.seqera.tower.model.DataLinksListResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.List;

@Command(
        name = "list",
        description = "List data links"
)
public class ListCmd extends AbstractDataLinksCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public PaginationOptions paginationOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier")
    public String credentialsRef;

    @CommandLine.Option(names = {"--wait"}, description = "Wait for all data links to be fetched to cache")
    public boolean wait;

    @CommandLine.Option(names = {"--visibility"}, description = "Filter by visibility: hidden, visible, or all")
    public Visibility visibilityOption;

    // Search params
    @CommandLine.Mixin
    public SearchOption searchOption;


    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);
        Long wspId = workspaceId(workspace.workspace);
        String credId = credentialsRef != null ? credentialsByRef(null, wspId, credentialsRef) : null;
        String provider = searchOption.providers == null ? null : formatProviders(searchOption.providers);
        String search = buildSearch(searchOption.startsWith, provider, searchOption.region, searchOption.uri);
        String visibility = visibilityOption == null ? null : visibilityOption.toString();

        DataLinkService dataLinkService = new DataLinkService(dataLinksApi(), app());
        boolean isResultIncomplete = dataLinkService.checkIfResultIncomplete(wspId, credId, wait);

        DataLinksListResponse data = dataLinksApi().listDataLinks(wspId, credId, search, max, offset, visibility);
        return new DataLinksList(workspaceRef(wspId), data.getDataLinks(),
                isResultIncomplete,
                PaginationInfo.from(offset, max, data.getTotalSize()));
    }

    private static String formatProviders(List<DataLinkProvider> providers) {
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (DataLinkProvider provider : providers) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(provider);
        }
        return builder.toString();
    }

    public static String buildSearch(String name, String providers, String region, String uri) {
        StringBuilder builder = new StringBuilder();
        if (name != null && !name.isBlank()) {
            builder.append(name);
        }
        if (providers != null && !providers.isBlank()) {
            appendParameter(builder, providers, "provider");
        }
        if (region != null && !region.isBlank()) {
            appendParameter(builder, region, "region");
        }
        if (uri != null && !uri.isBlank()) {
            appendParameter(builder, uri, "resourceRef");
        }
        if (builder.length() > 0)
            return builder.toString();
        return null;
    }

    private static void appendParameter(StringBuilder input, String param, String paramName) {
        if (param != null && !param.isBlank()) {
            if (input.length() > 0) {
                input.append(" ");
            }
            input.append(paramName).append(":").append(param);
        }
    }

    enum Visibility {
        hidden, visible, all
    }

}
