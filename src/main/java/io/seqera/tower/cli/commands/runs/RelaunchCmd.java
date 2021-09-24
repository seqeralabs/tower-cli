package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunCreated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.time.OffsetDateTime;

@Command(
        name = "relaunch",
        description = "Create a pipeline's run"
)
public class RelaunchCmd extends AbstractRunsCmd {

    @Option(names = {"-i", "--id"}, description = "Pipeline id to run", required = true)
    public Long id;

    @Option(names = {"-p", "--pipeline"}, description = "Pipeline to launch")
    public String pipeline;

    @Option(names = {"--no-resume"}, description = "Not to resume the pipeline's run (true as default)")
    public boolean resume = true;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Launch launch = launchByPipelineId(id);

        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest()
                .id(launch.getId())
                .computeEnvId(opts.computeEnv != null ? opts.computeEnv : launch.getComputeEnv().getId())
                .pipeline(pipeline != null ? pipeline : launch.getPipeline())
                .workDir(opts.workDir != null ? opts.workDir : launch.getWorkDir())
                .revision(opts.revision != null ? opts.revision : launch.getRevision())
                .configProfiles(opts.profiles != null ? opts.profiles : launch.getConfigProfiles())
                .configText(opts.config != null ? FilesHelper.readString(opts.config) : launch.getConfigText())
                .paramsText(opts.params != null ? FilesHelper.readString(opts.params) : launch.getParamsText())
                .preRunScript(opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : launch.getPreRunScript())
                .postRunScript(opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) :launch.getPostRunScript())
                .mainScript(opts.mainScript != null ? opts.mainScript :launch.getPostRunScript())
                .entryName(opts.entryName != null ? opts.entryName :launch.getEntryName())
                .schemaName(opts.schemaName != null ? opts.schemaName :launch.getSchemaName())
                .resume(resume)
                .pullLatest(opts.pullLatest != null ? opts.pullLatest :launch.getPullLatest())
                .stubRun(opts.stubRun != null ? opts.stubRun :launch.getStubRun())
                .dateCreated(OffsetDateTime.now());

        SubmitWorkflowLaunchRequest submitWorkflowLaunchRequest = new SubmitWorkflowLaunchRequest()
                .launch(workflowLaunchRequest);

        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(submitWorkflowLaunchRequest, workspaceId());

        return new RunCreated(response.getWorkflowId(), workspaceRef());
    }
}
