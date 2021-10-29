package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesCreated;
import io.seqera.tower.cli.utils.FilesHelper;
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

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to import", arity = "1")
    Path fileName = null;

    @Override
    protected Response exec() throws ApiException, IOException {
        CreatePipelineRequest request;

        request = parseJson(FilesHelper.readString(fileName), CreatePipelineRequest.class);
        request.setName(name);

        api().createPipeline(request, workspaceId());

        return new PipelinesCreated(workspaceRef(), name);
    }
}
