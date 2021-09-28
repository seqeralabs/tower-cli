package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.Workspaces.WorkspaceUpdated;
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
    @CommandLine.Mixin
    WorkspaceOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = orgAndWorkspaceByName(opts.workspaceName, opts.organizationName);

        UpdateWorkspaceRequest request = new UpdateWorkspaceRequest();
        request.setFullName(opts.workspaceFullName);
        request.setDescription(opts.description);
        request.setVisibility(Visibility.PRIVATE);

        DescribeWorkspaceResponse response = api().updateWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId(), request);

        return new WorkspaceUpdated(response.getWorkspace().getName(), opts.organizationName, response.getWorkspace().getVisibility());
    }
}

