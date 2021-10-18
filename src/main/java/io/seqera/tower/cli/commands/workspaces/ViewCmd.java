package io.seqera.tower.cli.commands.workspaces;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.WorkspaceView;
import io.seqera.tower.model.DescribeWorkspaceResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "view",
        description = "Describe an existing organization's workspace"
)
public class ViewCmd extends AbstractWorkspaceCmd {
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

        DescribeWorkspaceResponse response = api().describeWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceView(response.getWorkspace());
    }
}
