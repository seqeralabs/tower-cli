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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.AgentDbDto;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;
import static io.seqera.tower.cli.utils.FormatHelper.formatDescription;

public class AgentsList extends Response {

    public final String workspaceRef;
    public final List<AgentDbDto> agents;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public AgentsList(String workspaceRef, List<AgentDbDto> agents, @Nullable PaginationInfo paginationInfo) {
        this.workspaceRef = workspaceRef;
        this.agents = agents;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Agents at %s workspace:|@%n", workspaceRef)));
        if (agents == null || agents.isEmpty()) {
            out.println(ansi("    @|yellow No agents found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "ID", "Name", "Description", "Status", "Updated");
        table.setPrefix("    ");
        agents.forEach(agent -> table.addRow(
                agent.getId(),
                agent.getName(),
                formatDescription(agent.getDescription(), 100),
                agent.getStatus() == null ? "NA" : agent.getStatus().getValue(),
                formatDate(agent.getLastUpdated())
        ));
        table.print();
        PaginationInfo.addFooter(out, paginationInfo);
        out.println("");
    }
}
