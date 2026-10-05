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
import io.seqera.tower.model.AgentRunDbDto;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;
import static io.seqera.tower.cli.utils.FormatHelper.formatDescription;

public class AgentRunsList extends Response {

    public final String workspaceRef;
    public final List<AgentRunDbDto> agentRuns;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public AgentRunsList(String workspaceRef, List<AgentRunDbDto> agentRuns, @Nullable PaginationInfo paginationInfo) {
        this.workspaceRef = workspaceRef;
        this.agentRuns = agentRuns;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Agent runs at %s workspace:|@%n", workspaceRef)));
        if (agentRuns == null || agentRuns.isEmpty()) {
            out.println(ansi("    @|yellow No agent runs found|@"));
            return;
        }

        TableList table = new TableList(out, 6, "ID", "Title", "Status", "Trigger", "Run ID", "Created");
        table.setPrefix("    ");
        agentRuns.forEach(run -> table.addRow(
                run.getId(),
                formatDescription(run.getTitle(), 60),
                run.getStatus() == null ? "NA" : run.getStatus().getValue(),
                run.getTrigger() == null ? "" : Objects.toString(run.getTrigger().getType(), ""),
                Objects.toString(run.getWorkflowId(), ""),
                formatDate(run.getDateCreated())
        ));
        table.print();
        PaginationInfo.addFooter(out, paginationInfo);
        out.println("");
    }
}
