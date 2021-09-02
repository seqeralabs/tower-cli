package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.PipelinesCreated;
import io.seqera.tower.cli.responses.PipelinesUpdated;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.CreatePipelineResponse;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.UpdatePipelineRequest;
import io.seqera.tower.model.UpdatePipelineResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;

@Command(
        name = "update",
        description = "Update a workspace pipeline"
)
public class UpdateCmd extends AbstractPipelinesCmd {

    @Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @Option(names = {"-d", "--description"}, description = "Pipeline description")
    public String description;

    @Mixin
    public LaunchOptions opts;

    @Option(names = {"--pipeline"}, description = "Nextflow pipeline URL")
    public String pipeline;

    @Override
    protected Response exec() throws ApiException, IOException {

        PipelineDbDto pipe = pipelineByName(name);

        // Retrieve current launch
        Launch launch = api().describePipelineLaunch(pipe.getPipelineId(), workspaceId()).getLaunch();

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByName(opts.computeEnv) : launch.getComputeEnv();

        UpdatePipelineResponse response = api().updatePipeline(
                pipe.getPipelineId(),
                new UpdatePipelineRequest()
                        .description(coalesce(description, pipe.getDescription()))
                        .launch(new WorkflowLaunchRequest()
                                .computeEnvId(ce.getId())
                                .pipeline(coalesce(pipeline, launch.getPipeline()))
                                .revision(coalesce(opts.revision, launch.getRevision()))
                                .workDir(coalesce(opts.workDir, launch.getWorkDir()))
                                .configProfiles(coalesce(opts.profiles, launch.getConfigProfiles()))
                                .paramsText(coalesce(FilesHelper.readString(opts.params), launch.getParamsText()))

                                // Advanced options
                                .configText(coalesce(FilesHelper.readString(opts.config), launch.getConfigText()))
                                .preRunScript(coalesce(FilesHelper.readString(opts.preRunScript), launch.getPreRunScript()))
                                .postRunScript(coalesce(FilesHelper.readString(opts.postRunScript), launch.getPostRunScript()))
                                .pullLatest(coalesce(opts.pullLatest, launch.getPullLatest()))
                                .stubRun(coalesce(opts.stubRun, launch.getStubRun()))
                                .mainScript(coalesce(opts.mainScript, launch.getMainScript()))
                                .entryName(coalesce(opts.entryName, launch.getEntryName()))
                                .schemaName(coalesce(opts.schemaName, launch.getSchemaName()))
                        )
                , workspaceId()
        );

        return new PipelinesUpdated(workspaceRef(), response.getPipeline());
    }

    private static <T> T coalesce(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
