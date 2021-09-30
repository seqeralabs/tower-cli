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

    @CommandLine.ArgGroup
    public WorkspacesMatchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {

        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto;

        if (opts.matchById != null) {
            orgAndWorkspaceDbDto = workspaceById(opts.matchById.workspaceId);
        } else {
            orgAndWorkspaceDbDto = orgAndWorkspaceByName(opts.matchByName.workspaceName, opts.matchByName.organizationName);
        }

        api().deleteWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceDeleted(orgAndWorkspaceDbDto.getWorkspaceName(), orgAndWorkspaceDbDto.getOrgName());
    }
}
