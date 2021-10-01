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

        DescribeWorkspaceResponse response = api().describeWorkspace(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new WorkspaceView(response.getWorkspace());
    }
}
