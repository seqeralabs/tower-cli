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

