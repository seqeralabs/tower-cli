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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.AgentDbDto;
import picocli.CommandLine.Command;

@Command
public abstract class AbstractAgentsCmd extends AbstractApiCmd {

    // The API caps agent pages at 100 entries
    private static final int MAX_PAGE_SIZE = 100;

    protected AgentDbDto fetchAgent(AgentRefOptions.AgentRef ref, Long wspId) throws ApiException {
        String agentId = ref.id != null ? ref.id : agentByName(ref.name, wspId).getId();
        return agentsApi().describeAgent(agentId, wspId).getAgent();
    }

    private AgentDbDto agentByName(String name, Long wspId) throws ApiException {
        for (AgentDbDto agent : agentsApi().listAgents(wspId, name, MAX_PAGE_SIZE, 0).getAgents()) {
            if (name.equals(agent.getName())) {
                return agent;
            }
        }
        throw new TowerException(String.format("Unknown agent '%s' at %s workspace", name, workspaceRef(wspId)));
    }
}
