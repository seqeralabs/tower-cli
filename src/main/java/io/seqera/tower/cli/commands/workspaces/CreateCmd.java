package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.Workspaces.WorkspaceCreated;
import io.seqera.tower.model.CreateWorkspaceRequest;
import io.seqera.tower.model.CreateWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.Visibility;
import io.seqera.tower.model.Workspace;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;

@Command(
        name = "create",
        description = "Create a new organization's workspace"
)
public class CreateCmd extends AbstractWorkspaceCmd {
    @Mixin
    WorkspaceOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Workspace workspace = new Workspace();
        workspace.setName(opts.workspaceName);
        workspace.setFullName(opts.workspaceFullName);
        workspace.setDescription(opts.description);
        workspace.setVisibility(Visibility.PRIVATE);

        CreateWorkspaceRequest request = new CreateWorkspaceRequest().workspace(workspace);

        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = organizationByName(opts.organizationName);

        api().workspaceValidate(orgAndWorkspaceDbDto.getOrgId(), opts.workspaceName);

        CreateWorkspaceResponse response = api().createWorkspace(orgAndWorkspaceDbDto.getOrgId(), request);

        return new WorkspaceCreated(response.getWorkspace().getName(), opts.organizationName, response.getWorkspace().getVisibility());
    }
}