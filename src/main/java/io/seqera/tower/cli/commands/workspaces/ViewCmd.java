package io.seqera.tower.cli.commands.workspaces;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.Workspaces.WorkspaceView;
import io.seqera.tower.model.DescribeWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "view",
        description = "Describe an existing organization's workspace"
)
public class ViewCmd extends AbstractWorkspaceCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
    public String workspaceName;

    @CommandLine.Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
    public String organizationName;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = orgAndWorkspaceByName(workspaceName, organizationName);

        DescribeWorkspaceResponse response = api().describeWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceView(response.getWorkspace());
    }
}
