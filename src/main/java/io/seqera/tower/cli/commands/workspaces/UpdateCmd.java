/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceUpdated;
import io.seqera.tower.model.DescribeWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.UpdateWorkspaceRequest;
import io.seqera.tower.model.Visibility;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "update",
        description = "Update an existing organization's workspace"
)
public class UpdateCmd extends AbstractWorkspaceCmd {

    @Mixin
    public WorkspacesMatchOptions ws;

    @Option(names = {"-f", "--fullName"}, description = "The workspace full name")
    public String workspaceFullName;

    @Option(names = {"-d", "--description"}, description = "The workspace description")
    public String description;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto;

        if (ws.match.byId != null) {
            orgAndWorkspaceDbDto = workspaceById(ws.match.byId.workspaceId);
        } else {
            orgAndWorkspaceDbDto = orgAndWorkspaceByName(ws.match.byName.workspaceName, ws.match.byName.organizationName);
        }

        UpdateWorkspaceRequest request = new UpdateWorkspaceRequest();
        if (workspaceFullName != null) {
            request.setFullName(workspaceFullName);
        }
        request.setDescription(description);
        request.setVisibility(Visibility.PRIVATE);

        DescribeWorkspaceResponse response = api().updateWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId(), request);

        return new WorkspaceUpdated(response.getWorkspace().getName(), orgAndWorkspaceDbDto.getOrgName(), response.getWorkspace().getVisibility());
    }
}

