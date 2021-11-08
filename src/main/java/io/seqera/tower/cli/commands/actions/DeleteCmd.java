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
import io.seqera.tower.cli.commands.global.WorkspaceOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsDelete;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a Pipeline Action"
)
public class DeleteCmd extends AbstractActionsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Action name", required = true)
    public String actionName;

    @CommandLine.Mixin
    public WorkspaceOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponseActionInfo listActionsResponseActionInfo = actionByName(workspace.workspaceId, actionName);

        try {
            api().deleteAction(listActionsResponseActionInfo.getId(), workspace.workspaceId);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to delete action '%s' for workspace '%s'", actionName, workspaceRef(workspace.workspaceId)));
        }

        return new ActionsDelete(actionName, workspaceRef(workspace.workspaceId));
    }
}
