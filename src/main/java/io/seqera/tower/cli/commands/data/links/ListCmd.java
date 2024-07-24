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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.data.DataLinksList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.ResponseHelper;
import io.seqera.tower.model.DataLinksListResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        description = "List data links."
)
public class ListCmd extends AbstractApiCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public PaginationOptions paginationOptions;

    @CommandLine.Option(names = {"-c", "--credentials"}, description = "Credentials identifier.")
    public String credentialsRef;

    @CommandLine.Option(names = {"--wait"}, description = "When present program will wait till all data links are fetched to cache.")
    public boolean wait;

    @CommandLine.Option(names = {"--visibility"}, description = "Show  only data links that are [hidden, visible, all]. When not present all will be shown.")
    public Visibility visibilityOption;

    // Search params
    @CommandLine.Mixin
    public SearchOption searchOption;


    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);
        Long wspId = workspaceId(workspace.workspace);
        String provider = searchOption.providers == null ? null : searchOption.providers.toString();
        String search = buildSearch(searchOption.startsWith, provider, searchOption.region, searchOption.uri);
        String visibility = visibilityOption == null ? null : visibilityOption.toString();

        DataLinksFetchStatus status = checkDataLinksFetchStatus(wspId, credentialsRef);
        if (wait && status == DataLinksFetchStatus.FETCHING) {
            waitForDoneStatus(wspId, credentialsRef);
        }

        DataLinksListResponse data = api().listDataLinks(wspId, credentialsRef, search, max, offset, visibility);
        return new DataLinksList(workspaceRef(wspId), data.getDataLinks(),
                !wait && status == DataLinksFetchStatus.FETCHING,
                PaginationInfo.from(offset, max, data.getTotalSize()));
    }

    private void waitForDoneStatus(Long wspId, String credId) {
        try {
            ResponseHelper.waitStatus(
                    app().getOut(),
                    app().output != OutputType.json,
                    DataLinksFetchStatus.DONE,
                    DataLinksFetchStatus.values(),
                    () -> checkDataLinksFetchStatus(wspId, credId),
                    DataLinksFetchStatus.DONE, DataLinksFetchStatus.ERROR
            );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    DataLinksFetchStatus checkDataLinksFetchStatus(Long wspId, String credentialsId) {
        int status = 0;
        try {
            status = api().listDataLinksWithHttpInfo(wspId, credentialsId, null, 1, 0, null).getStatusCode();
        } catch (ApiException e) {
            return DataLinksFetchStatus.ERROR;
        }
        switch (status) {
            case 200:
                return DataLinksFetchStatus.DONE;
            case 202:
                return DataLinksFetchStatus.FETCHING;
            default:
                return DataLinksFetchStatus.ERROR;
        }
    }

    public enum DataLinksFetchStatus {
        FETCHING, DONE, ERROR
    }
}
