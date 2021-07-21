package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.App;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.autocomplete.WorkspacePipelinesCompletion;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import static io.seqera.tower.cli.ModelHelper.createLaunchRequest;

import java.io.File;
import java.net.URI;
import java.util.Optional;

@Command(name = "run", description = "Run a Nextflow pipeline")
public class RunCmd extends BaseCmd {

    @Parameters(index = "0", description = "workspace pipeline name or full pipeline URL", completionCandidates = WorkspacePipelinesCompletion.class)
    String pipeline;

    @Option(names = { "-params-file" }, description = "parameters file")
    Optional<File> paramsFile;

    public RunCmd(App app) {
        super(app);
    }

    @Override
    protected Integer exec() throws ApiException {

        // Run absolute URLs as Nextflow pipelines
        URI pipe = URI.create(pipeline);
        if (pipe.isAbsolute()) {
            return runNextflowPipeline();
        }

        // Otherwise run pipelines defined at current workspace
        return runTowerPipeline();

    }

    protected Integer runNextflowPipeline() throws ApiException {
        println("TODO: run nextflow pipeline");
        return 0;
    }

    protected Integer runTowerPipeline() throws ApiException {
        ListPipelinesResponse pipelines = api().pipelineWorkspaceList(workspaceId(), 2, 0, pipeline);
        if (pipelines.getTotalSize() == 0) {
            println(String.format("Pipeline '%s' not found on this workspace.", pipeline));
            return -1;
        }

        if (pipelines.getTotalSize() > 1) {
            println(String.format("Multiple pipelines match '%s'", pipeline));
            return -1;
        }

        Long pipelineId = pipelines.getPipelines().get(0).getPipelineId();
        Launch launch = api().pipelineLaunchDescribe(pipelineId, workspaceId()).getLaunch();

        return submitWorkflow(createLaunchRequest(launch));
    }

    protected Integer submitWorkflow(WorkflowLaunchRequest launch) throws ApiException {
        SubmitWorkflowLaunchResponse response = api().workflowLaunchSubmit(new SubmitWorkflowLaunchRequest().launch(launch), workspaceId());
        String workflowId = response.getWorkflowId();
        println(String.format("Workflow submitted. Check it here:%n%s", workflowWatchUrl(workflowId)));
        return 0;
    }

    private String workflowWatchUrl(String workflowId) {

        if (workspaceId() == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName(), workflowId);
        }

        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(), workspaceName(), workflowId);
    }

}


