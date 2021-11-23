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

package io.seqera.tower.cli.commands.actions.add;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionAdd;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ActionSource;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreateActionRequest;
import io.seqera.tower.model.CreateActionResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

import java.io.IOException;

public abstract class AbstractAddCmd extends AbstractApiCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name.", required = true)
    public String actionName;

    @CommandLine.Option(names = {"--pipeline"}, description = "Pipeline to launch.", required = true)
    public String pipeline;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByName(wspId, opts.computeEnv) : primaryComputeEnv(wspId);

        // Use compute env values by default
        String workDirValue = opts.workDir != null ? opts.workDir : ce.getConfig() != null ? ce.getConfig().getWorkDir() : null;
        String preRunScriptValue = opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : ce.getConfig() != null ? ce.getConfig().getPreRunScript() : null;
        String postRunScriptValue = opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : ce.getConfig() != null ? ce.getConfig().getPostRunScript() : null;


        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest();
        workflowLaunchRequest.computeEnvId(ce.getId())
                .pipeline(pipeline)
                .revision(opts.revision)
                .workDir(workDirValue)
                .configProfiles(opts.profiles)
                .paramsText(FilesHelper.readString(opts.params))

                // Advanced options
                .configText(FilesHelper.readString(opts.config))
                .preRunScript(preRunScriptValue)
                .postRunScript(postRunScriptValue)
                .pullLatest(opts.pullLatest)
                .stubRun(opts.stubRun)
                .mainScript(opts.mainScript)
                .entryName(opts.entryName);

        CreateActionRequest request = new CreateActionRequest();
        request.setName(actionName);
        request.setSource(getSource());
        request.setLaunch(workflowLaunchRequest);

        CreateActionResponse response;
        try {
            response = api().createAction(request, wspId);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to add action for workspace '%s'", workspaceRef(wspId)));
        }

        return new ActionAdd(actionName, workspaceRef(wspId), response.getActionId());
    }

    protected abstract ActionSource getSource();
}
