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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsDelete;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a Pipeline Action."
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
            api().deleteAction(id, wspId);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to delete action '%s' for workspace '%s'", actionRef, workspaceRef(wspId)));
        }

        return new ActionsDelete(actionRef, workspaceRef(wspId));
    }
}
