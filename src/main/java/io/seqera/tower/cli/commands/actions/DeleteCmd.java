package io.seqera.tower.cli.commands.actions;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsDelete;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

@CommandLine.Command(
        name = "delete",
        description = "Delete a Pipeline Action"
)
public class DeleteCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(actionName);

        try {
            api().deleteAction(listActionsResponseActionInfo.getId(), workspaceId());
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to delete action '%s' for workspace '%s'", actionName, workspaceRef()));
        }

        return new ActionsDelete(actionName, workspaceRef());
    }
}
