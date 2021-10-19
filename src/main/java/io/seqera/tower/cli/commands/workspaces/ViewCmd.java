package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceView;
import io.seqera.tower.model.DescribeWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;

@Command(
        name = "view",
        description = "Describe an existing organization's workspace"
)
public class ViewCmd extends AbstractWorkspaceCmd {

    @Mixin
    public WorkspacesMatchOptions ws;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto;

        if (ws.match.byId != null) {
            orgAndWorkspaceDbDto = workspaceById(ws.match.byId.workspaceId);
        } else {
            orgAndWorkspaceDbDto = orgAndWorkspaceByName(ws.match.byName.workspaceName, ws.match.byName.organizationName);
        }

        DescribeWorkspaceResponse response = api().describeWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceView(response.getWorkspace());
    }
}
