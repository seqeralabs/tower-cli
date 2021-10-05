package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsPause;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
        name = "pause",
        description = "Toggle the pause status of an existing Pipeline Action"
)
public class PauseCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @CommandLine.Option(names = {"--params"}, description = "Pipeline parameters using a JSON file")
    public Path params;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(actionName);

        String body = FilesHelper.readString(params);

        try {
            api().pauseAction(listActionsResponseActionInfo.getId(), workspaceId(), body);
        } catch (Exception e) {
            throw new TowerException(String.format("An error has occur while pausing action '%s'", actionName));
        }

        return new ActionsPause(actionName, workspaceRef());
    }
}
