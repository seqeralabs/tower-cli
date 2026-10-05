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
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.AgentRunStatusResponse;

import java.io.PrintWriter;
import java.util.Objects;

public class AgentRunView extends Response {

    public final String workspaceRef;
    public final AgentRunStatusResponse agentRun;

    public AgentRunView(String workspaceRef, AgentRunStatusResponse agentRun) {
        this.workspaceRef = workspaceRef;
        this.agentRun = agentRun;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Agent run at %s workspace:|@%n", workspaceRef)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", agentRun.getAgentRunId());
        table.addRow("Status", agentRun.getStatus());
        table.addRow("Thread ID", Objects.toString(agentRun.getThreadId(), ""));
        table.addRow("Session ID", Objects.toString(agentRun.getSessionId(), ""));
        table.print();
        out.println("");
    }
}
