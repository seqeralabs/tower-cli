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

package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceUpdated;
import io.seqera.tower.model.DescribeWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.UpdateWorkspaceRequest;
import io.seqera.tower.model.Visibility;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Command(
        name = "update",
        description = "Update a workspace"
)
public class UpdateCmd extends AbstractWorkspaceCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Workspace identifier", required = true)
    public Long workspaceId;

    @Option(names = {"--new-name"}, description = "Updated workspace name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters. Must be 2-40 characters.")
    public String workspaceNewName;

    @Option(names = {"-f", "--fullName"}, description = "Updated full display name for the workspace. Maximum 100 characters.")
    public String workspaceFullName;

    @Option(names = {"-d", "--description"}, description = "Updated workspace description. Maximum 1000 characters.")
    public String description;

    @Override
    protected Response exec() throws ApiException, IOException {

        boolean updates =
                workspaceFullName != null
                || workspaceNewName != null
                || description != null;

        if (!updates) {
            throw new ShowUsageException(getSpec(), "Required at least one option to update");
        }

        OrgAndWorkspaceDto ws = workspaceById(workspaceId);

        DescribeWorkspaceResponse response = workspacesApi().describeWorkspace(ws.getOrgId(), ws.getWorkspaceId());
        UpdateWorkspaceRequest request = new UpdateWorkspaceRequest()
                .fullName(response.getWorkspace().getFullName())
                .description(response.getWorkspace().getDescription());

        if (workspaceNewName != null) {
            request.setName(workspaceNewName);
        }

        if (workspaceFullName != null) {
            request.setFullName(workspaceFullName);
        }

        if (description != null) {
            request.setDescription(description);
        }

        request.setVisibility(Visibility.PRIVATE);
        workspacesApi().updateWorkspace(ws.getOrgId(), ws.getWorkspaceId(), request);

        return new WorkspaceUpdated(response.getWorkspace().getName(), ws.getOrgName(), response.getWorkspace().getVisibility());
    }
}

