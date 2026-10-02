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

package io.seqera.tower.cli.commands.actions.triggers;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.actions.AbstractActionsCmd;
import io.seqera.tower.cli.commands.actions.ActionRefOptions;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionTriggersList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ActionTriggerResult;
import io.seqera.tower.model.ListActionTriggersResponse;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "list",
        description = "List the triggers of a pipeline action, newest first"
)
public class ListCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    ActionRefOptions actionRefOptions;

    @CommandLine.Option(names = {"--outcome"}, split = ",", description = "Show only the triggers that ended one of these ways (comma-separated): ${COMPLETION-CANDIDATES}.")
    public List<ActionTriggerResult> outcomes;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        String actionId = actionId(actionRefOptions, wspId);
        ListActionTriggersResponse response = actionsApi().listActionTriggers(actionId, wspId, max, offset, outcomes, null, null);

        return new ActionTriggersList(actionId, workspaceRef(wspId), response.getTriggers(), baseWorkspaceUrl(wspId), PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }
}
