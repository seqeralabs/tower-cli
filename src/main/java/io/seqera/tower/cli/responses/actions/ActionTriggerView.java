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
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ActionTriggerResponseDto;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatTime;
import static io.seqera.tower.cli.utils.FormatHelper.formatUntrusted;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowId;

public class ActionTriggerView extends Response {

    public final ActionTriggerResponseDto trigger;

    @JsonIgnore
    private final String baseWorkspaceUrl;

    public ActionTriggerView(ActionTriggerResponseDto trigger, String baseWorkspaceUrl) {
        this.trigger = trigger;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Details for trigger '%s'|@%n", trigger.getId())));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", trigger.getId());
        table.addRow("Action ID", trigger.getActionId());
        table.addRow("Source", trigger.getSource() == null ? null : trigger.getSource().toString());
        table.addRow("Fired at", formatTime(trigger.getFiredAt()));
        table.addRow("Outcome", trigger.getOutcome() == null ? null : trigger.getOutcome().toString());
        table.addRow("Outcome detail", formatUntrusted(trigger.getOutcomeDetail()));
        table.addRow("Event", formatUntrusted(trigger.getEventSummary()));
        table.addRow("Run ID", trigger.getWorkflowId() == null ? null : formatWorkflowId(trigger.getWorkflowId(), baseWorkspaceUrl));
        table.addRow("Caused by trigger", trigger.getCausedByTriggerId());
        table.print();

        if (trigger.getEventPayload() != null) {
            out.println(String.format("%n  Event payload:%n%n%s%n", formatUntrusted(trigger.getEventPayload()).replaceAll("(?m)^", "     ")));
        }
    }
}
