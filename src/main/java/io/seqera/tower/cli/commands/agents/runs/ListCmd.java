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


package io.seqera.tower.cli.commands.agents.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.agents.AgentRunsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListAgentRunsResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "list",
        description = "List agent runs, newest first"
)
public class ListCmd extends AbstractApiCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @Option(names = {"-f", "--filter"}, description = "Optional filter criteria, allowing the keywords: `status` (pending, running, completed, failed), `agentConfigId`, `workflowId` and `sourcePipelineId`. Free text is not supported. Example keyword usage: -f status:failed.")
    public String filter;

    @Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListAgentRunsResponse response = agentsApi().listAgentRuns(wspId, filter, max, offset);

        return new AgentRunsList(workspaceRef(wspId), response.getAgentRuns(), PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }
}
