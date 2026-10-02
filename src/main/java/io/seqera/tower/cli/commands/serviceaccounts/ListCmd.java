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

package io.seqera.tower.cli.commands.serviceaccounts;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListServiceAccountsResponse;
import io.seqera.tower.model.ListWorkspaceServiceAccountsResponse;
import io.seqera.tower.model.ServiceAccountDto;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;

@Command(
        name = "list",
        description = "List service accounts"
)
public class ListCmd extends AbstractServiceAccountsCmd {

    @ArgGroup(multiplicity = "1")
    Scope scope;

    static class Scope {
        @Option(names = {"-o", "--organization"}, description = ORGANIZATION_DESCRIPTION)
        String organizationRef;

        @Option(names = {"-w", "--workspace"}, description = "Workspace numeric identifier or reference in OrganizationName/WorkspaceName format. Lists only the service accounts assigned to this workspace.")
        String workspaceRef;
    }

    @Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        if (scope.organizationRef != null) {
            Long orgId = findOrganizationByRef(scope.organizationRef).getOrgId();
            ListServiceAccountsResponse response = serviceAccountsApi().listServiceAccounts(orgId, offset, max);
            return new ServiceAccountsList(scope.organizationRef, null, response.getServiceAccounts(), PaginationInfo.from(paginationOptions, response.getTotalSize()));
        }

        Long wspId = workspaceId(scope.workspaceRef);
        List<ServiceAccountDto> all = listWorkspaceServiceAccounts(orgId(wspId), wspId);
        // The workspace endpoint pages with an opaque token, so offset and max are applied to the full list
        int from = Math.min(offset, all.size());
        int to = Math.min(from + max, all.size());
        return new ServiceAccountsList(orgName(wspId), workspaceName(wspId), all.subList(from, to), PaginationInfo.from(paginationOptions, (long) all.size()));
    }

    private List<ServiceAccountDto> listWorkspaceServiceAccounts(Long orgId, Long wspId) throws ApiException {
        List<ServiceAccountDto> result = new ArrayList<>();
        String pageToken = null;
        do {
            ListWorkspaceServiceAccountsResponse page = serviceAccountsApi().listWorkspaceServiceAccounts(orgId, wspId, pageToken, PaginationOptions.MAX);
            page.getItems().forEach(it -> result.add(new ServiceAccountDto()
                    .id(it.getId())
                    .name(it.getName())
                    .description(it.getDescription())
                    .createdAt(it.getCreatedAt())));
            pageToken = page.getNextPageToken();
        } while (pageToken != null);
        return result;
    }
}
