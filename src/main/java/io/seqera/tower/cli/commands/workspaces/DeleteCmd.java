package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceDeleted;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete an organization's workspace"
)
public class DeleteCmd extends AbstractWorkspaceCmd {

    @CommandLine.ArgGroup(exclusive = false, heading = "%nMatch by workspace and organization name:%n")
    WorkspacesMatchOptions.MatchByName matchByName;

    @CommandLine.ArgGroup(heading = "%nMatch by workspace ID:%n")
    WorkspacesMatchOptions.MatchById matchById;

    @Override
    protected Response exec() throws ApiException, IOException {

        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto;

        if (matchById != null) {
            orgAndWorkspaceDbDto = workspaceById(matchById.workspaceId);
        } else {
            orgAndWorkspaceDbDto = orgAndWorkspaceByName(matchByName.workspaceName, matchByName.organizationName);
        }

        api().deleteWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceDeleted(orgAndWorkspaceDbDto.getWorkspaceName(), orgAndWorkspaceDbDto.getOrgName());
    }
}
