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

package io.seqera.tower.cli.responses.pipelines;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import io.seqera.tower.model.PipelineDbDto;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.seqera.tower.cli.utils.FormatHelper.formatPipelineId;

public class PipelinesList extends Response {

    public final String workspaceRef;
    public final List<PipelineDbDto> pipelines;

    @JsonIgnore
    private final String baseWorkspaceUrl;

    @JsonIgnore
    private boolean includeLabels;

    public PipelinesList(String workspaceRef, List<PipelineDbDto> pipelines, String baseWorkspaceUrl, boolean includeLabels) {
        this.workspaceRef = workspaceRef;
        this.pipelines = pipelines;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.includeLabels = includeLabels;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Pipelines at %s workspace:|@%n", workspaceRef)));

        if (pipelines.isEmpty()) {
            out.println(ansi("    @|yellow No pipelines found|@"));
            return;
        }

        List<String> descriptions = new ArrayList<>(List.of("ID", "Name", "Repository", "Visibility"));
        if (includeLabels) descriptions.add("Labels");

        TableList table = new TableList(out, descriptions.size(), descriptions.toArray(new String[descriptions.size()])).sortBy(0);
        table.setPrefix("    ");
        pipelines.forEach(pipe -> {

            List<String> rows = new ArrayList<>(List.of(
                    formatPipelineId(pipe.getPipelineId(), baseWorkspaceUrl),
                    pipe.getName(),
                    pipe.getRepository(),
                    pipe.getVisibility() == null ? "" : pipe.getVisibility()
            ));
            if (includeLabels) rows.add(commaSeparated(pipe));

            table.addRow(rows.toArray(new String[rows.size()]));
        });
        table.print();
        out.println("");
    }

    private String commaSeparated(final PipelineDbDto res) {
        if (res.getLabels() == null || res.getLabels().isEmpty()) {
            return "";
        }
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
