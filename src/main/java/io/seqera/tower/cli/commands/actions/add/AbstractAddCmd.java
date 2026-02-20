/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.actions.add;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.actions.AbstractActionsCmd;
import io.seqera.tower.cli.commands.actions.ActionsLabelsManager;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.labels.Label;
import io.seqera.tower.cli.commands.labels.LabelsOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.exceptions.ActionNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionAdd;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ActionSource;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.CreateActionRequest;
import io.seqera.tower.model.CreateActionResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;

public abstract class AbstractAddCmd extends AbstractActionsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name.", required = true)
    public String actionName;

    @CommandLine.Option(names = {"--pipeline"}, description = "Pipeline to launch.", required = true)
    public String pipeline;

    @CommandLine.Mixin
    public LabelsOptionalOptions labels;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public LaunchOptions opts;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the action if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnvResponseDto ce = opts.computeEnv != null ? computeEnvByRef(wspId, opts.computeEnv) : primaryComputeEnv(wspId);

        // Use compute env values by default
        String workDirValue = opts.workDir != null ? opts.workDir : ce.getConfig() != null ? ce.getConfig().getWorkDir() : null;
        String preRunScriptValue = opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : ce.getConfig() != null ? ce.getConfig().getPreRunScript() : null;
        String postRunScriptValue = opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : ce.getConfig() != null ? ce.getConfig().getPostRunScript() : null;


        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest();
        workflowLaunchRequest.computeEnvId(ce.getId())
                .pipeline(pipeline)
                .revision(opts.revision)
                .workDir(workDirValue)
                .configProfiles(opts.profile)
                .paramsText(FilesHelper.readString(opts.paramsFile))

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

        if (overwrite) tryDeleteAction(actionName, wspId);

        CreateActionResponse response;
        try {
            response = actionsApi().createAction(request, wspId);
            attachLabels(labels.labels,wspId,response.getActionId());
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to add action for workspace '%s'", workspaceRef(wspId)));
        }

        return new ActionAdd(actionName, workspaceRef(wspId), response.getActionId());
    }

    private void attachLabels(List<Label> labels,Long wspId, String actionId) throws ApiException {
        ActionsLabelsManager creator = new ActionsLabelsManager(labelsApi());
        creator.execute(wspId,actionId, labels);
    }

    private void tryDeleteAction(String actionName, Long wspId) throws ApiException {
        try {
            deleteActionByName(actionName, wspId);
        } catch (ActionNotFoundException ignored){}
    }

    protected abstract ActionSource getSource();
}
