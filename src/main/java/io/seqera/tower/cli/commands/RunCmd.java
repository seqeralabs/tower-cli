package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunSubmited;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.cli.utils.InvalidResponseException;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static io.seqera.tower.cli.utils.ModelHelper.createLaunchRequest;

@Command(name = "run", description = "Run a Nextflow pipeline")
public class RunCmd extends AbstractRootCmd {

    @Parameters(index = "0", paramLabel = "PIPELINE_OR_URL", description = "Workspace pipeline name or full pipeline URL", arity = "1")
    String pipeline;

    @Option(names = {"--params"}, description = "Parameters file")
    Path paramsFile;

    @Option(names = {"-c", "--compute-env"}, description = "Compute environment name (defaults to workspace primary)")
    String computeEnv;

    @Option(names = {"-w", "--work-dir"}, description = "Working directory")
    String workDir;

    @Option(names = {"-p", "--profile"}, split = ",", description = "Configuration profiles")
    List<String> profile;

    public RunCmd() {
    }

    @Override
    protected Response exec() throws ApiException, IOException {

        // If the pipeline has at least one backslash consider it an external pipeline.
        if (pipeline.contains("/")) {
            return runNextflowPipeline();
        }

        // Otherwise run pipelines defined at current workspace
        return runTowerPipeline();

    }

    protected Response runNextflowPipeline() throws ApiException, IOException {

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = computeEnv != null ? computeEnvByName(computeEnv) : primaryComputeEnv();

        return submitWorkflow( new WorkflowLaunchRequest()
                .pipeline(pipeline)
                .workDir(workDir != null ? workDir : ce.getConfig().getWorkDir())
                .computeEnvId(ce.getId())
                .configProfiles(profile)
        );
    }

    protected Response runTowerPipeline() throws ApiException, IOException {
        ListPipelinesResponse pipelines = api().listPipelines(workspaceId(), 2, 0, pipeline);
        if (pipelines.getTotalSize() == 0) {
            throw new InvalidResponseException(String.format("Pipeline '%s' not found on this workspace.", pipeline));
        }

        if (pipelines.getTotalSize() > 1) {
            throw new InvalidResponseException(String.format("Multiple pipelines match '%s'", pipeline));
        }

        Long pipelineId = pipelines.getPipelines().get(0).getPipelineId();
        Launch launch = api().describePipelineLaunch(pipelineId, workspaceId()).getLaunch();

        return submitWorkflow(createLaunchRequest(launch));
    }

    protected Response submitWorkflow(WorkflowLaunchRequest launch) throws ApiException, IOException {

        if (paramsFile != null) {
            launch.paramsText(FilesHelper.readString(paramsFile));
        }

        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(new SubmitWorkflowLaunchRequest().launch(launch), workspaceId());
        String workflowId = response.getWorkflowId();
        return new RunSubmited(workflowId, workflowWatchUrl(workflowId), workspaceRef());
    }

    private String workflowWatchUrl(String workflowId) throws ApiException {

        if (workspaceId() == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName(), workflowId);
        }

        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(), workspaceName(), workflowId);
    }

}


