package io.seqera.tower.cli.commands.runs.tasks;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunTasksView;
import io.seqera.tower.model.ListTasksResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;

@CommandLine.Command(
        name = "tasks",
        description = "Display pipeline's run task details"
)
public class TasksCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Filters by process name")
    public List<Object> params;

    @CommandLine.ParentCommand
    public ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListTasksResponse response = api().listWorkflowTasks(parentCommand.id, parentCommand.workspace.workspaceId, params);

        return new RunTasksView(response.getTasks());
    }
}
