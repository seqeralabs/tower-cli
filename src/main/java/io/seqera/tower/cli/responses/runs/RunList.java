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

package io.seqera.tower.cli.responses.runs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListWorkflowsResponseListWorkflowsElement;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.seqera.tower.cli.utils.FormatHelper.formatTime;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowId;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowStatus;

public class RunList extends Response {

    public final String workspaceRef;
    public final List<ListWorkflowsResponseListWorkflowsElement> workflows;

    public boolean showLabels;

    @JsonIgnore
    public final String baseWorkspaceUrl;

    public RunList(String workspaceRef, List<ListWorkflowsResponseListWorkflowsElement> runs, String baseWorkspaceUrl, boolean showLabels) {
        this.workspaceRef = workspaceRef;
        this.workflows = runs;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.showLabels = showLabels;
    }

    public RunList(String workspaceRef, List<ListWorkflowsResponseListWorkflowsElement> runs, String baseWorkspaceUrl) {
        this.workspaceRef = workspaceRef;
        this.workflows = runs;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.showLabels = false;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Pipeline runs at %s workspace:|@%n", workspaceRef)));

        if (workflows.isEmpty()) {
            out.println(ansi("    @|yellow No pipeline runs found|@"));
            return;
        }

        List<String> desc = new ArrayList<>(List.of("ID", "Status", "Project Name", "Run Name", "Username", "Submit Date"));
        if (showLabels) desc.add("Labels");

        String[] descriptions = new String[desc.size()];
        desc.toArray(descriptions);

        TableList table = new TableList(out, descriptions.length, descriptions);
        table.setPrefix("    ");

        workflows.forEach(wf -> {

            List<String> rows = new ArrayList<>(List.of(
                formatWorkflowId(wf.getWorkflow().getId(), this.baseWorkspaceUrl),
                formatWorkflowStatus(wf.getWorkflow().getStatus()),
                wf.getWorkflow().getProjectName(),
                wf.getWorkflow().getRunName(),
                wf.getWorkflow().getUserName(),
                formatTime(wf.getWorkflow().getSubmit())
            ));
            if (showLabels) rows.add(getLabelRow(wf));

            String[] rowsArray = new String[rows.size()];
            rows.toArray(rowsArray);
            table.addRow(rowsArray);
        });

        table.print();
        out.println("");
    }

    private String getLabelRow(ListWorkflowsResponseListWorkflowsElement e) {
        return e.getLabels()
                .stream()
                .map(label -> {
                    String str = label.getName();
                    if (label.getValue() != null && !label.getValue().isEmpty()) {
                        str += "=" + label.getValue();
                    }
                    return str;
                })
                .collect(Collectors.joining(","));
    }
}
