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

        List<ActionQueryAttribute> actionQueryAttributes = showLabelsOption.showLabels ? List.of(ActionQueryAttribute.labels) : NO_ACTION_ATTRIBUTES;

        ListActionsResponse response = actionsApi().listActions(wspId, actionQueryAttributes);

        return new ActionsList(response.getActions(), userName(), baseWorkspaceUrl(wspId), showLabelsOption.showLabels);
    }
}

