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


package io.seqera.tower.cli.commands.agents;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.agents.AgentsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListAgentsResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "list",
        description = "List agents"
)
public class ListCmd extends AbstractAgentsCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @Option(names = {"-f", "--filter"}, description = "Filter agents by name")
    public String filter;

    @Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListAgentsResponse response = agentsApi().listAgents(wspId, filter, max, offset);

        return new AgentsList(workspaceRef(wspId), response.getAgents(), PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }
}
