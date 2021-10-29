package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesExport;
import io.seqera.tower.cli.utils.ModelHelper;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Export a workspace pipeline for further creation"
)
public class ExportCmd extends AbstractPipelinesCmd{

    @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to export", arity = "0..1")
    String fileName = null;

    @Override
    protected Response exec() throws ApiException {
        PipelineDbDto pipeline = pipelineByName(name);
        DescribeLaunchResponse resp = api().describePipelineLaunch(pipeline.getPipelineId(), workspaceId());

        WorkflowLaunchRequest workflowLaunchRequest = ModelHelper.createLaunchRequest(resp.getLaunch());

        CreatePipelineRequest createPipelineRequest = new CreatePipelineRequest();
        createPipelineRequest.setDescription(pipeline.getDescription());
        createPipelineRequest.setIcon(pipeline.getIcon());
        createPipelineRequest.setLaunch(workflowLaunchRequest);

        return new PipelinesExport(createPipelineRequest, fileName);
    }
}
