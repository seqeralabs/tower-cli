package io.seqera.tower.cli.commands.actions;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsList;
import io.seqera.tower.model.ListActionsResponse;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        description = "List the available Pipeline Actions for the authenticated user or given workspace"
)
public class ListCmd extends AbstractActionsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponse response = api().listActions(workspaceId());

        return new ActionsList(response.getActions(), userName());
    }
}

