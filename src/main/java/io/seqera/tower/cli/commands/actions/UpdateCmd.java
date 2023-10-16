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

package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionUpdate;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ActionResponseDto;
import io.seqera.tower.model.UpdateActionRequest;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Objects;

@CommandLine.Command(
        name = "update",
        description = "Update a Pipeline Action."
)
public class UpdateCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    ActionRefOptions actionRefOptions;

    @CommandLine.Option(names = {"-s", "--status"}, description = "Action status (pause or active).")
    public String status;

    @CommandLine.Option(names = {"--new-name"}, description = "Action new name.")
    public String newName;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        ActionResponseDto action = fetchDescribeActionResponse(actionRefOptions, wspId).getAction();
        String actionName = action.getName();

        // Validate new action name if any
        if (newName != null) {
            try {
                api().validateActionName(wspId, newName);
            } catch (ApiException ex) {
                throw new InvalidResponseException(String.format("Action name '%s' is not valid", newName));
            }
        }

        // Retrieve the provided computeEnv or use the primary if not provided
        String ceId = opts.computeEnv != null ? computeEnvByRef(wspId, opts.computeEnv).getId() : action.getLaunch().getComputeEnv().getId();

        // Use compute env values by default
        String workDirValue = opts.workDir != null ? opts.workDir : action.getLaunch().getWorkDir();
        String preRunScriptValue = opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : action.getLaunch().getPreRunScript();
        String postRunScriptValue = opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : action.getLaunch().getPostRunScript();


        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest();
        workflowLaunchRequest.computeEnvId(ceId)
                .id(action.getLaunch().getId())
                .pipeline(action.getLaunch().getPipeline())
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

        UpdateActionRequest request = new UpdateActionRequest();
        request.setName(newName != null ? newName : actionName);
        request.setLaunch(workflowLaunchRequest);

        try {
            api().updateAction(action.getId(), request, wspId);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to update action '%s' for workspace '%s'", actionName, workspaceRef(wspId)));
        }

        if (status != null) {
            if (Objects.equals(action.getStatus().toString().toLowerCase(), status)) {
                throw new TowerException(String.format("The action is already set to '%s'", status.toUpperCase()));
            }

            try {
                api().pauseAction(action.getId(), wspId, null);
            } catch (Exception e) {
                throw new TowerException(String.format("An error has occur while setting the action '%s' to '%s'", actionName, status.toUpperCase()));
            }
        }

        return new ActionUpdate(actionName, workspaceRef(wspId), action.getId());
    }
}
