package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesExport;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.PipelineDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Export a workspace pipeline for further creation"
)
public class ExportCmd extends AbstractPipelinesCmd{

    @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @CommandLine.Option(names = {"--format"}, description = "Export file to json or yml format (default is json)")
    public String format = "json";

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to export", arity = "1")
    String fileName = null;

    @Override
    protected Response exec() throws ApiException {
        PipelineDbDto pipe = pipelineByName(name);
        DescribeLaunchResponse resp = api().describePipelineLaunch(pipe.getPipelineId(), workspaceId());

        return new PipelinesExport(pipe, resp.getLaunch(), format, fileName);
    }
}
