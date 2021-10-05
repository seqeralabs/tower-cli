package io.seqera.tower.cli.commands.actions;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsDelete;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

@CommandLine.Command(
        name = "delete",
        description = "Delete an existing Pipeline Action"
)
public class DeleteCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(actionName);

        api().deleteAction(listActionsResponseActionInfo.getId(), workspaceId());

        return new ActionsDelete(actionName, workspaceRef());
    }
}
