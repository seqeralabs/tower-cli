/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.responses.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.ModelHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ActionResponseDto;
import io.seqera.tower.model.WorkflowLaunchRequest;

import java.io.PrintWriter;
import java.util.stream.Collectors;

import static io.seqera.tower.cli.utils.FormatHelper.formatActionId;
import static io.seqera.tower.cli.utils.FormatHelper.formatActionStatus;
import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;

public class ActionsView extends Response {

    final public ActionResponseDto action;

    @JsonIgnore
    private String baseWorkspaceUrl;

    public ActionsView(ActionResponseDto action, String baseWorkspaceUrl) {
        this.action = action;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public void toString(PrintWriter out) {
        String configJson = "";
        try {
            WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(action.getLaunch());
            configJson = new JSON().getContext(WorkflowLaunchRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        out.println(ansi(String.format("%n  @|bold Details for action '%s'|@%n", action.getName())));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", formatActionId(action.getId(), baseWorkspaceUrl));
        table.addRow("Name", action.getName());
        table.addRow("Status", formatActionStatus(action.getStatus()));
        table.addRow("Pipeline URL", action.getLaunch().getPipeline());
        table.addRow("Source", action.getSource().toString());
        table.addRow("Hook URL", action.getHookUrl());
        table.addRow("Last event", FormatHelper.formatTime(action.getLastSeen()));
        table.addRow("Date created", FormatHelper.formatTime(action.getDateCreated()));
        table.addRow("Last event", FormatHelper.formatTime(action.getLastSeen()));
        table.addRow("Labels", action.getLabels() == null || action.getLabels().isEmpty() ? "No labels found" : formatLabels(action.getLabels()));

        table.print();

        out.println(String.format("%n  Configuration:%n%n%s%n", configJson.replaceAll("(?m)^", "     ")));

    }

}
