package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.Tower;
import io.seqera.tower.ApiException;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import static io.seqera.tower.cli.ModelHelper.createLaunchRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Command(name = "run", description = "Run a Nextflow pipeline")
public class RunCmd extends BaseCmd {

    @ParentCommand
    protected Tower app;

    @Parameters(index = "0", description = "workspace pipeline name or full pipeline URL")
    String pipeline;

    @Option(names = { "--params-file" }, description = "parameters file")
    Path paramsFile;

    @Option(names = { "--compute-env" }, description = "compute environment name")
    String computeEnv;

    @Option(names = { "-w", "--work-dir" }, description = "working directory")
    String workDir;

    @Option(names = { "-p", "--profile" }, split = ",")
    String[] profile;

    public RunCmd() {}

    @Override
    protected Tower app() {
        return app;
    }

    @Override
    protected Integer exec() throws ApiException, IOException {

        // If the pipeline has at least one backslash consider it an external pipeline.
        if (pipeline.contains("/")) {
            return runNextflowPipeline();
        }

        // Otherwise run pipelines defined at current workspace
        return runTowerPipeline();

    }

    protected Integer runNextflowPipeline() throws ApiException, IOException {

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = computeEnv != null ? computeEnvByName(computeEnv) : primaryComputeEnv();

        WorkflowLaunchRequest launch = new WorkflowLaunchRequest()
                .pipeline(pipeline)
                .workDir(workDir != null ? workDir : ce.getConfig().getWorkDir() )
                .computeEnvId(ce.getId());

        if (profile != null) {
            launch.configProfiles(Arrays.asList(profile));
        }

        return submitWorkflow(launch);
    }

    protected Integer runTowerPipeline() throws ApiException, IOException {
        ListPipelinesResponse pipelines = api().listPipelines(workspaceId(), 2, 0, pipeline);
        if (pipelines.getTotalSize() == 0) {
            println(String.format("Pipeline '%s' not found on this workspace.", pipeline));
            return -1;
        }

        if (pipelines.getTotalSize() > 1) {
            println(String.format("Multiple pipelines match '%s'", pipeline));
            return -1;
        }

        Long pipelineId = pipelines.getPipelines().get(0).getPipelineId();
        Launch launch = api().describePipelineLaunch(pipelineId, workspaceId()).getLaunch();

        return submitWorkflow(createLaunchRequest(launch));
    }

    protected Integer submitWorkflow(WorkflowLaunchRequest launch) throws ApiException, IOException {

        if (paramsFile != null) {
            launch.paramsText(Files.readString(paramsFile));
        }

        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(new SubmitWorkflowLaunchRequest().launch(launch), workspaceId());
        String workflowId = response.getWorkflowId();
        println(String.format("Workflow submitted. Check it here:%n%s", workflowWatchUrl(workflowId)));
        return 0;
    }

    private String workflowWatchUrl(String workflowId) throws ApiException {

        if (workspaceId() == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName(), workflowId);
        }

        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(), workspaceName(), workflowId);
    }

}


