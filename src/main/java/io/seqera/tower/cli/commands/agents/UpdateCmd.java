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
import io.seqera.tower.model.UpdateAgentRequest;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;

import static io.seqera.tower.cli.utils.ModelHelper.coalesce;

@Command(
        name = "update",
        description = "Update an agent"
)
public class UpdateCmd extends AbstractAgentsCmd {

    @Mixin
    public WorkspaceRequiredOptions workspace;

    @Mixin
    AgentRefOptions ref;

    @Option(names = {"--new-name"}, description = "New agent name. Names consist of alphanumeric, hyphen, and underscore characters.")
    public String newName;

    @Option(names = {"-d", "--description"}, description = "Agent description (max 120 characters).")
    public String description;

    @ArgGroup
    public AgentInstructionsOptions instructions;

    @Option(names = {"--service-account-id"}, description = "Service account user ID. The agent runs with the permissions of this service account in the workspace.")
    public Long serviceAccountId;

    @Option(names = {"--github-app-credentials-id"}, description = "GitHub App credentials identifier. Lets the agent clone, commit, and push using these credentials.")
    public String githubAppCredentialsId;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        AgentDbDto agent = fetchAgent(ref.agent, wspId);

        // The API replaces the whole agent, so unchanged fields are sent with their current value
        UpdateAgentRequest request = new UpdateAgentRequest()
                .name(coalesce(newName, agent.getName()))
                .description(coalesce(description, agent.getDescription()))
                .agentInstructions(instructions != null ? instructions.read() : agent.getAgentInstructions())
                .agentInstructionsTemplateId(agent.getAgentInstructionsTemplateId())
                .serviceAccountId(coalesce(serviceAccountId, agent.getServiceAccountId()))
                .githubAppCredentialId(coalesce(githubAppCredentialsId, agent.getGithubAppCredentialId()));

        AgentDbDto updated = agentsApi().updateAgent(agent.getId(), request, wspId).getAgent();
        return new AgentUpdated(workspaceRef(wspId), updated.getName(), "updated");
    }
}
