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
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatTime;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowId;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowStatus;

public class RunList extends Response {

    public final String workspaceRef;
    public final List<ListWorkflowsResponseListWorkflowsElement> workflows;

    @JsonIgnore
    public final String baseWorkspaceUrl;

    public RunList(String workspaceRef, List<ListWorkflowsResponseListWorkflowsElement> runs, String baseWorkspaceUrl) {
        this.workspaceRef = workspaceRef;
        this.workflows = runs;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Pipeline runs at %s workspace:|@%n", workspaceRef)));

        if (workflows.isEmpty()) {
            out.println(ansi("    @|yellow No pipeline runs found|@"));
            return;
        }

        TableList table = new TableList(out, 6, "ID", "Status", "Project Name", "Run Name", "Username", "Submit Date");
        table.setPrefix("    ");
        workflows.forEach(wf -> table.addRow(
                formatWorkflowId(wf.getWorkflow().getId(), this.baseWorkspaceUrl),
                formatWorkflowStatus(wf.getWorkflow().getStatus()),
                wf.getWorkflow().getProjectName(),
                wf.getWorkflow().getRunName(),
                wf.getWorkflow().getUserName(),
                formatTime(wf.getWorkflow().getSubmit())
        ));
        table.print();
        out.println("");
    }
}
