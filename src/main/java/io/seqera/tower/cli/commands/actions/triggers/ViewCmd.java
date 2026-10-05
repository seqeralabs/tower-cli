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
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.actions.ActionTriggerView;
import picocli.CommandLine;

@CommandLine.Command(
        name = "view",
        description = "View a trigger of a pipeline action, including the event that caused it"
)
public class ViewCmd extends AbstractActionsCmd {

    @CommandLine.Mixin
    ActionRefOptions actionRefOptions;

    @CommandLine.Option(names = {"--trigger-id"}, description = "Trigger unique identifier.", required = true)
    public String triggerId;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String actionId = actionId(actionRefOptions, wspId);

        return new ActionTriggerView(actionsApi().describeActionTrigger(actionId, triggerId, wspId).getTrigger(), baseWorkspaceUrl(wspId));
    }
}
