package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.responses.pipelines.PipelinesList;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ListPipelinesResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        description = "List workspace pipelines"
)
public class ListCmd extends AbstractPipelinesCmd {

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only pipelines that contain the given word")
    public String filter;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListPipelinesResponse response = api().listPipelines(workspaceId(), max, offset, filter);
        return new PipelinesList(workspaceRef(), response.getPipelines());
    }
}
