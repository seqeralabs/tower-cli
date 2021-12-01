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

package io.seqera.tower.cli.responses.computeenvs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatComputeEnvId;
import static io.seqera.tower.cli.utils.FormatHelper.formatComputeEnvStatus;
import static io.seqera.tower.cli.utils.FormatHelper.formatTime;

public class ComputeEnvList extends Response {

    public final String workspaceRef;
    public final List<ListComputeEnvsResponseEntry> computeEnvs;

    @JsonIgnore
    private String baseWorkspaceUrl;

    public ComputeEnvList(String workspaceRef, List<ListComputeEnvsResponseEntry> computeEnvs, String baseWorkspaceUrl) {
        this.workspaceRef = workspaceRef;
        this.computeEnvs = computeEnvs;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Compute environments at %s workspace:|@%n", workspaceRef)));

        if (computeEnvs.isEmpty()) {
            out.println(ansi("    @|yellow No compute environment found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "  ID", "Status", "Platform", "Name", "Last activity").sortBy(0);
        table.setPrefix("    ");
        computeEnvs.forEach(ce -> table.addRow(
                String.format("%s %s", ce.getPrimary() != null && ce.getPrimary() ? "*" : " ", formatComputeEnvId(ce.getId(), baseWorkspaceUrl)),
                formatComputeEnvStatus(ce.getStatus()),
                ce.getPlatform(),
                ce.getName(),
                formatTime(ce.getLastUsed())));
        table.print();
        out.println("");
    }
}
