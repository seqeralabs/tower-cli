package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesCreated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreatePipelineRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;

@CommandLine.Command(
        name = "import",
        description = "Create a workspace pipeline from file content"
)
public class ImportCmd extends AbstractPipelinesCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @CommandLine.Option(names = {"-c", "--compute-env"}, description = "Compute environment name (defaults to json file defined environment)")
    public String computeEnv;

    @CommandLine.Option(names = {"-w", "--workspace"}, description = "Workspace ID to create new pipeline", required = true)
    public Long workspaceId = null;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to import", arity = "1")
    Path fileName = null;

    @Override
    protected Response exec() throws ApiException, IOException {
        CreatePipelineRequest request;

        request = parseJson(FilesHelper.readString(fileName), CreatePipelineRequest.class);
        request.setName(name);

        if (computeEnv != null) {
            ComputeEnv ce = findComputeEnvironmentByName(computeEnv, workspaceId);
            request.getLaunch().setComputeEnvId(ce.getId());
        }

        api().createPipeline(request, workspaceId);

        return new PipelinesCreated(workspaceId.toString(), name);
    }
}