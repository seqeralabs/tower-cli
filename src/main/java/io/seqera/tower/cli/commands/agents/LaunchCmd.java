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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.agents.AgentLaunched;
import io.seqera.tower.model.AgentDbDto;
import io.seqera.tower.model.LaunchAgentRequest;
import io.seqera.tower.model.LaunchAgentResponse;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

import java.io.IOException;

@Command(
        name = "launch",
        description = "Launch a background agent run. Specify a configured agent (--id or --name), instructions, or both. Given instructions override the agent's own; without an agent, the run acts as the current user."
)
public class LaunchCmd extends AbstractAgentsCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @ArgGroup
    public AgentRefOptions.AgentRef agentRef;

    @ArgGroup
    public AgentInstructionsOptions instructions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        LaunchAgentRequest request = new LaunchAgentRequest();

        if (agentRef != null) {
            AgentDbDto agent = fetchAgent(agentRef, wspId);
            request.agentConfigId(agent.getId()).instructions(agent.getAgentInstructions());
        }
        if (instructions != null) {
            request.instructions(instructions.read());
        }
        if (request.getInstructions() == null) {
            throw new TowerException("Specify an agent to launch (--id or --name) or the instructions to run (--instructions or --instructions-file)");
        }

        LaunchAgentResponse response = agentsApi().launchAgent(request, wspId);
        return new AgentLaunched(workspaceRef(wspId), response.getAgentRunId(), response.getStatus());
    }
}
