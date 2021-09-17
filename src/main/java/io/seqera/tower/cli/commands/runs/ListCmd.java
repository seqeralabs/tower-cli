package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunList;
import io.seqera.tower.model.ListWorkflowsResponse;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "list",
        description = "List all pipeline's runs"
)
public class ListCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only pipeline's runs that contain the given word")
    public String filter;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListWorkflowsResponse response = api().listWorkflows(workspaceId(), null, null, filter);
        return new RunList(workspaceRef(), response.getWorkflows());
    }
}
