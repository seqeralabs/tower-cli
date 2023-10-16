/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.commands.workspaces;


import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceList;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Command(
        name = "list",
        description = "List user workspaces."
)
public class ListCmd extends AbstractWorkspaceCmd {

    @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "The workspace organization name.")
    public String organizationName;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListWorkspacesAndOrgResponse response = api().listWorkspacesUser(userId());

        if (response.getOrgsAndWorkspaces() == null) {
            return new WorkspaceList(userName(), Collections.emptyList(), serverUrl());
        }

        List<OrgAndWorkspaceDto> responseOrg = response.getOrgsAndWorkspaces()
                .stream()
                .filter(v -> v.getWorkspaceId() != null && (organizationName == null || organizationName.equals(v.getOrgName()))
                ).collect(Collectors.toList());

        return new WorkspaceList(userName(), responseOrg, serverUrl());
    }
}
