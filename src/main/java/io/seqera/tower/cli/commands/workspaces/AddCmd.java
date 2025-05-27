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

package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceAdded;
import io.seqera.tower.model.CreateWorkspaceRequest;
import io.seqera.tower.model.CreateWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.Visibility;
import io.seqera.tower.model.Workspace;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "add",
        description = "Add a new organization workspace."
)
public class AddCmd extends AbstractWorkspaceCmd {

    @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "The workspace organization name.", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-n", "--name"}, description = "The workspace short name. Only alphanumeric, dash and underscore characters are allowed.", required = true)
    public String workspaceName;

    @CommandLine.Option(names = {"-f", "--full-name"}, description = "The workspace full name.", required = true)
    public String workspaceFullName;

    @CommandLine.Option(names = {"-d", "--description"}, description = "The workspace description.")
    public String description;

    @CommandLine.Option(names = {"-v", "--visibility"}, description = "The workspace visibility. Valid options PRIVATE, SHARED [default: PRIVATE].")
    public String visibility = "PRIVATE";

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the workspace if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {

        OrgAndWorkspaceDto orgWspDto;
        if (overwrite) { // check also wsp existence
            orgWspDto = findOrgAndWorkspaceByName(organizationName, workspaceName);
            tryDeleteWsp(orgWspDto.getWorkspaceId(), orgWspDto.getOrgId());
        } else { // normal 'add'
            orgWspDto = organizationByName(organizationName);
        }

        Workspace workspace = new Workspace();
        workspace.setName(workspaceName);
        workspace.setFullName(workspaceFullName);
        workspace.setDescription(description);
        workspace.setVisibility(parseVisibility());

        CreateWorkspaceRequest request = new CreateWorkspaceRequest().workspace(workspace);

        workspacesApi().workspaceValidate(orgWspDto.getOrgId(), workspaceName);
        CreateWorkspaceResponse response = workspacesApi().createWorkspace(orgWspDto.getOrgId(), request);

        return new WorkspaceAdded(response.getWorkspace().getName(), organizationName, response.getWorkspace().getVisibility());
    }

    private Visibility parseVisibility() throws ApiException {
        if ("PRIVATE".equals(visibility)) {
            return Visibility.PRIVATE;
        } else if ("SHARED".equals(visibility)) {
            return Visibility.SHARED;
        } else {
            throw new ApiException(
                    String.format("Invalid value for option '--visibility': expected one of [PRIVATE, SHARED] (case-sensitive) but was '%s'", visibility)
            );
        }
    }

    private void tryDeleteWsp(Long wspId, Long orgId) throws ApiException {
        try {
            deleteWorkspaceById(wspId, orgId);
        }catch (WorkspaceNotFoundException ignored) {}
    }
}
