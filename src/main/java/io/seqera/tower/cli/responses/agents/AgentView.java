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
import io.seqera.tower.model.AgentDbDto;

import java.io.PrintWriter;
import java.util.Objects;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class AgentView extends Response {

    public final String workspaceRef;
    public final AgentDbDto agent;

    public AgentView(String workspaceRef, AgentDbDto agent) {
        this.workspaceRef = workspaceRef;
        this.agent = agent;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Agent at %s workspace:|@%n", workspaceRef)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", agent.getId());
        table.addRow("Name", agent.getName());
        table.addRow("Description", Objects.toString(agent.getDescription(), ""));
        table.addRow("Status", agent.getStatus() == null ? "NA" : agent.getStatus().getValue());
        table.addRow("Created", formatDate(agent.getDateCreated()));
        table.addRow("Updated", formatDate(agent.getLastUpdated()));
        table.print();

        out.println(ansi(String.format("%n  @|bold Instructions:|@%n")));
        out.println(agent.getAgentInstructions());
        out.println("");
    }
}
