package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.LaunchNotFoundException;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.exceptions.WorkflowProgressNotFoundException;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.GetProgressResponse;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import picocli.CommandLine.Command;

@Command
abstract public class AbstractRunsCmd extends AbstractApiCmd {

    public AbstractRunsCmd() {
    }

    protected Workflow workflowById(String id) throws ApiException {
        DescribeWorkflowResponse workflowResponse = api().describeWorkflow(id, workspaceId());

        if (workflowResponse == null) {
            throw new RunNotFoundException(id, workspaceRef());
        }

        return workflowResponse.getWorkflow();
    }

    protected Launch launchById(String id) throws ApiException {
        DescribeLaunchResponse launchResponse = api().describeLaunch(id, workspaceId());

        if (launchResponse == null) {
            throw new LaunchNotFoundException(id, workspaceRef());
        }

        return launchResponse.getLaunch();
    }

    protected WorkflowLoad workflowLoadByWorkflowId(String id) throws ApiException {
        GetProgressResponse getProgressResponse = api().describeWorkflowProgress(id, workspaceId());

        if (getProgressResponse == null) {
            throw new WorkflowProgressNotFoundException(id, workspaceRef());
        }

        return getProgressResponse.getProgress().getWorkflowProgress();
    }
}
