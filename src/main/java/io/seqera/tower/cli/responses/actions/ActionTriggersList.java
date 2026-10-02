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

package io.seqera.tower.cli.responses.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ActionTriggerDto;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatTime;
import static io.seqera.tower.cli.utils.FormatHelper.formatUntrusted;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowId;

public class ActionTriggersList extends Response {

    public final String actionId;
    public final String workspaceRef;
    public final List<ActionTriggerDto> triggers;

    @JsonIgnore
    private final String baseWorkspaceUrl;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public ActionTriggersList(String actionId, String workspaceRef, List<ActionTriggerDto> triggers, String baseWorkspaceUrl, @Nullable PaginationInfo paginationInfo) {
        this.actionId = actionId;
        this.workspaceRef = workspaceRef;
        this.triggers = triggers;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Triggers of action '%s' at %s workspace:|@%n", actionId, workspaceRef)));

        if (triggers.isEmpty()) {
            out.println(ansi("    @|yellow No triggers found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "ID", "Fired at", "Outcome", "Event", "Run ID");
        table.setPrefix("    ");

        triggers.forEach(trigger -> table.addRow(
                trigger.getId(),
                formatTime(trigger.getFiredAt()),
                trigger.getOutcome() == null ? null : trigger.getOutcome().toString(),
                formatUntrusted(trigger.getEventSummary()),
                trigger.getWorkflowId() == null ? null : formatWorkflowId(trigger.getWorkflowId(), baseWorkspaceUrl)
        ));

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
