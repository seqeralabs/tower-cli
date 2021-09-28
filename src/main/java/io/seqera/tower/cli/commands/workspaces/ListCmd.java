package io.seqera.tower.cli.commands.workspaces;


import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.Workspaces.WorkspaceList;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        description = "List a user workspaces"
)
public class ListCmd extends AbstractWorkspaceCmd {

    @Override
    protected Response exec() throws ApiException, IOException {
        ListWorkspacesAndOrgResponse response = api().listWorkspacesUser(userId());
        return new WorkspaceList(userName(), response.getOrgsAndWorkspaces());
    }
}
