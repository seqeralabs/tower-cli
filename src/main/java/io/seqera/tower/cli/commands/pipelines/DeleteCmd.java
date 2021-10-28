package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.pipelines.PipelinesDeleted;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.PipelineDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "delete",
        description = "Delete a workspace pipeline"
)
public class DeleteCmd extends AbstractPipelinesCmd {

    @Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @Override
    protected Response exec() throws ApiException, IOException {
        PipelineDbDto pipe = pipelineByName(name);
        api().deletePipeline(pipe.getPipelineId(), workspaceId());
        return new PipelinesDeleted(pipe.getName(), workspaceRef());
    }
}
