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

package io.seqera.tower.cli.responses.runs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListWorkflowsResponseListWorkflowsElement;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatTime;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowId;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowStatus;
import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;

public class RunList extends Response {

    public final String workspaceRef;
    public final List<ListWorkflowsResponseListWorkflowsElement> workflows;

    public boolean showLabels;

    @JsonIgnore
    public final String baseWorkspaceUrl;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public RunList(String workspaceRef, List<ListWorkflowsResponseListWorkflowsElement> runs, String baseWorkspaceUrl, boolean showLabels, @Nullable PaginationInfo paginationInfo) {
        this.workspaceRef = workspaceRef;
        this.workflows = runs;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.showLabels = showLabels;
        this.paginationInfo = paginationInfo;
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
                wf.getWorkflow().getProjectName() == null ? "" : wf.getWorkflow().getProjectName(),
                wf.getWorkflow().getRunName(),
                wf.getWorkflow().getUserName() == null ? "" : wf.getWorkflow().getUserName(),
                formatTime(wf.getWorkflow().getSubmit())
            ));
            if (showLabels) rows.add(formatLabels(wf.getLabels()));

            String[] rowsArray = new String[rows.size()];
            rows.toArray(rowsArray);
            table.addRow(rowsArray);
        });

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }

}
