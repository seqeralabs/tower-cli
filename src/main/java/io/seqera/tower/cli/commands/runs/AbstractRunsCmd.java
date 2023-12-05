/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.LaunchNotFoundException;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.exceptions.WorkflowProgressNotFoundException;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.GetProgressResponse;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.WorkflowLoad;
import picocli.CommandLine.Command;

@Command
abstract public class AbstractRunsCmd extends AbstractApiCmd {

    public AbstractRunsCmd() {
    }

    protected DescribeWorkflowResponse workflowById(Long workspaceId, String id) throws ApiException {
        DescribeWorkflowResponse workflowResponse = api().describeWorkflow(id, workspaceId, NO_WORKFLOW_ATTRIBUTES);

        if (workflowResponse == null) {
            throw new RunNotFoundException(id, workspaceRef(workspaceId));
        }

        return workflowResponse;
    }

    protected DescribeWorkflowLaunchResponse workflowLaunchById(Long workspaceId, String workflowId) throws ApiException {
        DescribeWorkflowLaunchResponse wfLaunchResponse = api().describeWorkflowLaunch(workflowId, workspaceId);

        if (wfLaunchResponse == null) {
            throw new ApiException(String.format("Workflow '%s' launch not found at %d workspace", workflowId, workspaceId));
        }

        return wfLaunchResponse;
    }

    protected Launch launchById(Long workspaceId, String id) throws ApiException {
        DescribeLaunchResponse launchResponse = api().describeLaunch(id, workspaceId);

        if (launchResponse == null) {
            throw new LaunchNotFoundException(id, workspaceRef(workspaceId));
        }

        return launchResponse.getLaunch();
    }

    protected WorkflowLoad workflowLoadByWorkflowId(Long workspaceId, String id) throws ApiException {
        GetProgressResponse getProgressResponse = api().describeWorkflowProgress(id, workspaceId);

        if (getProgressResponse == null) {
            throw new WorkflowProgressNotFoundException(id, workspaceRef(workspaceId));
        }

        return getProgressResponse.getProgress().getWorkflowProgress();
    }
}
