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
import io.seqera.tower.model.ListComputeEnvsResponseEntry;

import java.io.PrintWriter;
import java.util.List;

public class ComputeEnvList extends Response {

    public final String workspaceRef;
    public final List<ListComputeEnvsResponseEntry> computeEnvs;

    public ComputeEnvList(String workspaceRef, List<ListComputeEnvsResponseEntry> computeEnvs) {
        this.workspaceRef = workspaceRef;
        this.computeEnvs = computeEnvs;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Compute environments at %s workspace:|@%n", workspaceRef)));

        if (computeEnvs.isEmpty()) {
            out.println(ansi("    @|yellow No compute environment found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "ID", "Status", "Platform", "Name", "Last activity").sortBy(0);
        table.setPrefix("    ");
        computeEnvs.forEach(ce -> table.addRow(String.format("%s %s", ce.getPrimary() != null && ce.getPrimary() ? "*" : "", ce.getId()), ce.getStatus().getValue(), ce.getPlatform(), ce.getName(), formatTime(ce.getLastUsed())));
        table.print();
        out.println("");
    }
}
