package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsLaunch;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.cli.utils.JsonHelper;
import io.seqera.tower.model.LaunchActionRequest;
import io.seqera.tower.model.LaunchActionResponse;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@CommandLine.Command(
        name = "launch",
        description = "Trigger the execution of a Tower Launch Action"
)
public class LaunchCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @CommandLine.Option(names = {"--params"}, description = "Pipeline parameters using a JSON file")
    public Path params;

    @Override
    protected Response exec() throws ApiException, IOException {
        String json = FilesHelper.readString(params);

        LaunchActionRequest request = new LaunchActionRequest();

        // TODO: Allow YML to map parsing
        if (params != null) {
            request.setParams(JsonHelper.parseJson(json, Map.class));
        }

        ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(actionName);

        LaunchActionResponse response;

        try {
            response = api().launchAction(listActionsResponseActionInfo.getId(), request, workspaceId());
        } catch (Exception e) {
            throw new TowerException(String.format("An error has occur while launching action '%s'", actionName));
        }

        return new ActionsLaunch(actionName, workspaceRef(), response.getWorkflowId());
    }
}
