package io.seqera.tower.cli.commands.actions;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsView;
import io.seqera.tower.model.DescribeActionResponse;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

@CommandLine.Command(
        name = "view",
        description = "Describe an existing Pipeline Action"
)
public class ViewCmd extends AbstractActionsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(actionName);

        DescribeActionResponse response = api().describeAction(listActionsResponseActionInfo.getId(), workspaceId());

        return new ActionsView(response.getAction());
    }
}
