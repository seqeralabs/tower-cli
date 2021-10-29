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
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunView;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.GetProgressResponse;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import picocli.CommandLine;

@CommandLine.Command(
        name = "view",
        description = "View pipeline's runs"
)
public class ViewCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline's run identifier", required = true)
    public String id;

    protected Response exec() throws ApiException {
        try {
            Workflow workflow = workflowById(id);

            Launch launch = launchById(workflow.getLaunchId());

            WorkflowLoad workflowLoad = workflowLoadByWorkflowId(id);
            ComputeEnv computeEnv = launch.getComputeEnv();

            return new RunView(id, workspaceRef(), workflow, workflowLoad, computeEnv);

        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new RunNotFoundException(id, workspaceRef());
            }

            throw e;
        }
    }
}
