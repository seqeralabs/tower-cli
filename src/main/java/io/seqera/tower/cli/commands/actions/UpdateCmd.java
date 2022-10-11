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

package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
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

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        ActionResponseDto action = fetchDescribeActionResponse(actionRefOptions, wspId).getAction();
        String actionName = action.getName();

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
