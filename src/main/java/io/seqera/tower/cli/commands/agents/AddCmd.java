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
import io.seqera.tower.cli.responses.agents.AgentAdded;
import io.seqera.tower.model.AgentDbDto;
import io.seqera.tower.model.CreateAgentRequest;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "add",
        description = "Add an agent"
)
public class AddCmd extends AbstractAgentsCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @Option(names = {"-n", "--name"}, description = "Agent name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.", required = true)
    public String name;

    @Option(names = {"-d", "--description"}, description = "Agent description (max 120 characters).")
    public String description;

    @ArgGroup(multiplicity = "1")
    public AgentInstructionsOptions instructions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        CreateAgentRequest request = new CreateAgentRequest()
                .name(name)
                .description(description)
                .agentInstructions(instructions.read());

        AgentDbDto agent = agentsApi().createAgent(request, wspId).getAgent();
        return new AgentAdded(workspaceRef(wspId), agent);
    }
}
