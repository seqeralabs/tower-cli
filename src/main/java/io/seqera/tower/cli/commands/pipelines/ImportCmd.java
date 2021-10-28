package io.seqera.tower.cli.commands.pipelines;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesCreated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.CreatePipelineRequest;
import picocli.CommandLine;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static io.seqera.tower.cli.utils.YmlHelper.parseYml;

@CommandLine.Command(
        name = "import",
        description = "Create a workspace pipeline from file content"
)
public class ImportCmd extends AbstractPipelinesCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @CommandLine.Option(names = {"--format"}, description = "Import data from json or yml format (default is json)")
    public String format = "json";

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to import", arity = "1")
    Path fileName = null;

    @Override
    protected Response exec() throws ApiException, IOException {
        CreatePipelineRequest request;

        if (Objects.equals(format, "yml")) {
            request = parseYml(FilesHelper.readString(fileName), CreatePipelineRequest.class);
        } else {
            request = parseJson(FilesHelper.readString(fileName), CreatePipelineRequest.class);
        }
        request.setName(name);

        api().createPipeline(request, workspaceId());

        return new PipelinesCreated(workspaceRef(), name);
    }
}
