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
import io.seqera.tower.cli.commands.global.ShowLabelsOption;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionsList;
import io.seqera.tower.model.ActionQueryAttribute;
import io.seqera.tower.model.ListActionsResponse;
import io.seqera.tower.model.ListLabelsResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(
        name = "list",
        description = "List the available Pipeline Actions for the authenticated user or given workspace."
)
public class ListCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public ShowLabelsOption showLabelsOption;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        List<ActionQueryAttribute> actionQueryAttributes = showLabelsOption.showLabels ? List.of(ActionQueryAttribute.LABELS) : NO_ACTION_ATTRIBUTES;

        ListActionsResponse response = api().listActions(wspId, actionQueryAttributes);

        return new ActionsList(response.getActions(), userName(), baseWorkspaceUrl(wspId), showLabelsOption.showLabels);
    }
}

