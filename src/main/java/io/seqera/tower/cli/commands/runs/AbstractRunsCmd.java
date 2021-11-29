/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

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

    protected DescribeWorkflowResponse workflowById(Long workspaceId, String id) throws ApiException {
        DescribeWorkflowResponse workflowResponse = api().describeWorkflow(id, workspaceId);

        if (workflowResponse == null) {
            throw new RunNotFoundException(id, workspaceRef(workspaceId));
        }

        return workflowResponse;
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
