package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceUpdated;
import io.seqera.tower.model.DescribeWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.UpdateWorkspaceRequest;
import io.seqera.tower.model.Visibility;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "update",
        description = "Update an existing organization's workspace"
)
public class UpdateCmd extends AbstractWorkspaceCmd {
    @CommandLine.ArgGroup( exclusive = false, heading = "Update by workspace and organization name")
    UpdateByName updateByName;

    @CommandLine.ArgGroup(heading = "Update by workspace ID")
    UpdateById updateById;

    @CommandLine.Option(names = {"-f", "--fullName"}, description = "The workspace full name")
    public String workspaceFullName;

    @CommandLine.Option(names = {"-d", "--description"}, description = "The workspace description")
    public String description;

    static class UpdateByName {
        @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
        public String workspaceName;

        @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
        public String organizationName;
    }

    static class UpdateById {
        @CommandLine.Option(names = {"-i", "--id"}, description = "Workspace id", required = true)
        public Long workspaceId;
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto;

        if (updateById != null) {
            orgAndWorkspaceDbDto = workspaceById(updateById.workspaceId);
        } else {
            orgAndWorkspaceDbDto = orgAndWorkspaceByName(updateByName.workspaceName, updateByName.organizationName);
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

