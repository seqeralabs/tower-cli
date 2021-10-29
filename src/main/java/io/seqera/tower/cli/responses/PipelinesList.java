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

package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineDbDto;

import java.io.PrintWriter;
import java.util.List;

public class PipelinesList extends Response {

    public final String workspaceRef;
    public final List<PipelineDbDto> pipelines;

    public PipelinesList(String workspaceRef, List<PipelineDbDto> pipelines) {
        this.workspaceRef = workspaceRef;
        this.pipelines = pipelines;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Pipelines at %s workspace:|@%n", workspaceRef)));

        if (pipelines.isEmpty()) {
            out.println(ansi("    @|yellow No pipelines found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "ID", "Repository", "Description").sortBy(0);
        table.setPrefix("    ");
        pipelines.forEach(pipe -> table.addRow(pipe.getName(), pipe.getRepository(), pipe.getDescription()));
        table.print();
        out.println("");
    }


}
