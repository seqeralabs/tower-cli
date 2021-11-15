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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsList;
import io.seqera.tower.model.ListActionsResponse;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "list",
        description = "List the available Pipeline Actions for the authenticated user or given workspace"
)
public class ListCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListActionsResponse response = api().listActions(workspace.workspaceId);

        return new ActionsList(response.getActions(), userName());
    }
}

