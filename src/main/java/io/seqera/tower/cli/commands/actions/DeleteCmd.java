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

package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsDelete;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a pipeline action"
)
public class DeleteCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    ActionRefOptions actionRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        String id;
        String actionRef;
        Long wspId = workspaceId(workspace.workspace);

        if (actionRefOptions.action.actionId != null) {
            id = actionRefOptions.action.actionId;
            actionRef = actionRefOptions.action.actionId;
        } else {
            ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(wspId, actionRefOptions.action.actionName);
            id = listActionsResponseActionInfo.getId();
            actionRef = listActionsResponseActionInfo.getName();
        }

        try {
            deleteActionById(id, wspId);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to delete action '%s' for workspace '%s'", actionRef, workspaceRef(wspId)));
        }

        return new ActionsDelete(actionRef, workspaceRef(wspId));
    }
}
