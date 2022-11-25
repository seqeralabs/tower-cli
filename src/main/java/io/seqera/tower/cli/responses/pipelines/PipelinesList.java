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
import io.seqera.tower.model.PipelineDbDto;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatPipelineId;

public class PipelinesList extends Response {

    public final String workspaceRef;
    public final List<PipelineDbDto> pipelines;

    @JsonIgnore
    private final String baseWorkspaceUrl;

    public PipelinesList(String workspaceRef, List<PipelineDbDto> pipelines, String baseWorkspaceUrl) {
        this.workspaceRef = workspaceRef;
        this.pipelines = pipelines;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Pipelines at %s workspace:|@%n", workspaceRef)));

        if (pipelines.isEmpty()) {
            out.println(ansi("    @|yellow No pipelines found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Name", "Repository", "Visibility").sortBy(0);
        table.setPrefix("    ");
        pipelines.forEach(pipe -> table.addRow(
                formatPipelineId(pipe.getPipelineId(), baseWorkspaceUrl),
                pipe.getName(),
                pipe.getRepository(),
                pipe.getVisibility()
        ));
        table.print();
        out.println("");
    }


}
