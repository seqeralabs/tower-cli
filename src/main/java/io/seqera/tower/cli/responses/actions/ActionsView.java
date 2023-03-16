/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
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

public class ActionsView extends Response {

    final public ActionResponseDto action;

    @JsonIgnore
    private String baseWorkspaceUrl;

    @JsonIgnore
    private boolean includeBoolean;

    public ActionsView(ActionResponseDto action, String baseWorkspaceUrl, boolean includeLabels) {
        this.action = action;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.includeBoolean = includeLabels;
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
        if (includeBoolean) table.addRow("Labels", action.getLabels().isEmpty() ? "No labels found" : commaSeparatedLabels(action));

        table.print();

        out.println(String.format("%n  Configuration:%n%n%s%n", configJson.replaceAll("(?m)^", "     ")));

    }

    private String commaSeparatedLabels(final ActionResponseDto res) {
        return res.getLabels().stream().map(label -> {
            String str = label.getName();
            if (label.getValue() != null && !label.getValue().isEmpty()) {
                str += "=" + label.getValue();
            }
            return str;
        })
        .collect(Collectors.joining(","));
    }


}
