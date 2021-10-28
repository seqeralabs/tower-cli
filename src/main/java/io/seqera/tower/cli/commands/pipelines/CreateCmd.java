package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.pipelines.PipelinesCreated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.CreatePipelineResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;

@Command(
        name = "create",
        description = "Create a workspace pipeline"
)
public class CreateCmd extends AbstractPipelinesCmd {

    @Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @Option(names = {"-d", "--description"}, description = "Pipeline description")
    public String description;

    @Parameters(index = "0", paramLabel = "PIPELINE_URL", description = "Nextflow pipeline URL", arity = "1")
    public String pipeline;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByName(opts.computeEnv) : primaryComputeEnv();

        // Use compute env values by default
        String workDirValue = opts.workDir == null ? ce.getConfig().getWorkDir() : opts.workDir;
        String preRunScriptValue = opts.preRunScript == null ? ce.getConfig().getPreRunScript() : FilesHelper.readString(opts.preRunScript);
        String postRunScriptValue = opts.postRunScript == null ? ce.getConfig().getPostRunScript() : FilesHelper.readString(opts.postRunScript);

        CreatePipelineResponse response = api().createPipeline(
                new CreatePipelineRequest()
                        .name(name)
                        .description(description)
                        .launch(new WorkflowLaunchRequest()
                                .computeEnvId(ce.getId())
                                .pipeline(pipeline)
                                .revision(opts.revision)
                                .workDir(workDirValue)
                                .configProfiles(opts.profiles)
                                .paramsText(FilesHelper.readString(opts.params))

                                // Advanced options
                                .configText(FilesHelper.readString(opts.config))
                                .preRunScript(preRunScriptValue)
                                .postRunScript(postRunScriptValue)
                                .pullLatest(opts.pullLatest)
                                .stubRun(opts.stubRun)
                                .mainScript(opts.mainScript)
                                .entryName(opts.entryName)
                                .schemaName(opts.schemaName)
                        )
                , workspaceId()
        );

        return new PipelinesCreated(workspaceRef(), response.getPipeline().getName());
    }
}
