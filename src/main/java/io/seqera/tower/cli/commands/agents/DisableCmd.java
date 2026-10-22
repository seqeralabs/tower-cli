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
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.agents.AgentUpdated;
import io.seqera.tower.model.AgentDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "disable",
        description = "Disable an active agent. A disabled agent cannot be launched."
)
public class DisableCmd extends AbstractAgentsCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @Mixin
    AgentRefOptions ref;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        AgentDbDto agent = fetchAgent(ref.agent, wspId);
        agentsApi().disableAgent(agent.getId(), wspId);
        return new AgentUpdated(workspaceRef(wspId), agent.getName(), "disabled");
    }
}
