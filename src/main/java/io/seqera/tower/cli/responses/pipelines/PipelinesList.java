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
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineDbDto;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatPipelineId;
import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;

public class PipelinesList extends Response {

    public final String workspaceRef;
    public final List<PipelineDbDto> pipelines;

    @JsonIgnore
    private final String baseWorkspaceUrl;

    @JsonIgnore
    private boolean includeLabels;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public PipelinesList(String workspaceRef, List<PipelineDbDto> pipelines, String baseWorkspaceUrl, boolean includeLabels, @Nullable PaginationInfo paginationInfo) {
        this.workspaceRef = workspaceRef;
        this.pipelines = pipelines;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
        this.includeLabels = includeLabels;
        this.paginationInfo = paginationInfo;
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
            if (includeLabels) rows.add(formatLabels(pipe.getLabels()));

            table.addRow(rows.toArray(new String[rows.size()]));
        });

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }

}
