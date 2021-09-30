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
    @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
    public String workspaceName;

    @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
    public String organizationName;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = orgAndWorkspaceByName(workspaceName, organizationName);

        api().deleteWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceDeleted(workspaceName, organizationName);
    }
}
