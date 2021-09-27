package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunSubmited;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.Workflow;
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

    @Option(names = {"-i", "--id"}, description = "Pipeline's run id to relaunch", required = true)
    public String id;

    @Option(names = {"--pipeline"}, description = "Pipeline to launch")
    public String pipeline;

    @Option(names = {"--no-resume"}, description = "Not to resume the pipeline's run (true as default)")
    public boolean resume = true;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Workflow workflow = workflowById(id);
        Launch launch = launchById(workflow.getLaunchId());

        ComputeEnv ce = null;
        if (opts.computeEnv != null) {
            ce = computeEnvByName(opts.computeEnv);
        }

        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest()
                .id(workflow.getLaunchId())
                .sessionId(launch.getSessionId())
                .computeEnvId(ce != null ? ce.getId() : launch.getComputeEnv().getId())
                .pipeline(pipeline != null ? pipeline : launch.getPipeline())
                .workDir(opts.workDir != null ? opts.workDir : launch.getWorkDir())
                .revision(opts.revision != null ? opts.revision : launch.getRevision())
                .configProfiles(opts.profiles != null ? opts.profiles : launch.getConfigProfiles())
                .configText(opts.config != null ? FilesHelper.readString(opts.config) : launch.getConfigText())
                .paramsText(opts.params != null ? FilesHelper.readString(opts.params) : launch.getParamsText())
                .preRunScript(opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : launch.getPreRunScript())
                .postRunScript(opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : launch.getPostRunScript())
                .mainScript(opts.mainScript != null ? opts.mainScript : launch.getPostRunScript())
                .entryName(opts.entryName != null ? opts.entryName : launch.getEntryName())
                .schemaName(opts.schemaName != null ? opts.schemaName : launch.getSchemaName())
                .resume(resume)
                .pullLatest(opts.pullLatest != null ? opts.pullLatest : launch.getPullLatest())
                .stubRun(opts.stubRun != null ? opts.stubRun : launch.getStubRun())
                .dateCreated(OffsetDateTime.now());

        SubmitWorkflowLaunchRequest submitWorkflowLaunchRequest = new SubmitWorkflowLaunchRequest()
                .launch(workflowLaunchRequest);

        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(submitWorkflowLaunchRequest, workspaceId());

        return new RunSubmited(response.getWorkflowId(), workflowWatchUrl(response.getWorkflowId()), workspaceRef());
    }

    private String workflowWatchUrl(String workflowId) throws ApiException {

        if (workspaceId() == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName(), workflowId);
        }

        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(), workspaceName(), workflowId);
    }
}
