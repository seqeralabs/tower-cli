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


package io.seqera.tower.cli.responses.agents;

import io.seqera.tower.cli.responses.Response;

public class AgentUpdated extends Response {

    public final String workspaceRef;
    public final String agentName;
    public final String action;

    public AgentUpdated(String workspaceRef, String agentName, String action) {
        this.workspaceRef = workspaceRef;
        this.agentName = agentName;
        this.action = action;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Agent '%s' %s at %s workspace|@%n", agentName, action, workspaceRef));
    }
}
