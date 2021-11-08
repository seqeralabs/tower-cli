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
import io.seqera.tower.cli.commands.global.WorkspaceOptions;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunView;
import io.seqera.tower.model.ComputeEnv;
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

    @CommandLine.Mixin
    public WorkspaceOptions workspace;

    protected Response exec() throws ApiException {
        try {
            Workflow workflow = workflowById(workspace.workspaceId, id);

            Launch launch = launchById(workspace.workspaceId, workflow.getLaunchId());

            WorkflowLoad workflowLoad = workflowLoadByWorkflowId(workspace.workspaceId, id);
            ComputeEnv computeEnv = launch.getComputeEnv();

            return new RunView(id, workspaceRef(workspace.workspaceId), workflow, workflowLoad, computeEnv);

        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new RunNotFoundException(id, workspaceRef(workspace.workspaceId));
            }

            throw e;
        }
    }
}
