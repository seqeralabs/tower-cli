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

package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsList;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "list",
        description = "List organizations"
)
public class ListCmd extends AbstractOrganizationsCmd { 
    @Override
    protected Response exec() throws ApiException, IOException {

        ListWorkspacesAndOrgResponse response = workspacesApi().listWorkspacesUser(userId());


        if (response.getOrgsAndWorkspaces() != null) {
            List<OrgAndWorkspaceDto> responseOrg = response.getOrgsAndWorkspaces()
                    .stream()
                    .filter(item -> Objects.equals(item.getWorkspaceId(), null))
                    .collect(Collectors.toList());

            return new OrganizationsList(userName(), responseOrg, serverUrl());
        }

        return new OrganizationsList(userName(), response.getOrgsAndWorkspaces(), serverUrl());
    }
}
