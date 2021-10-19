package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceDeleted;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;

@Command(
        name = "delete",
        description = "Delete an organization's workspace"
)
public class DeleteCmd extends AbstractWorkspaceCmd {

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

        api().deleteWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceDeleted(orgAndWorkspaceDbDto.getWorkspaceName(), orgAndWorkspaceDbDto.getOrgName());
    }
}
