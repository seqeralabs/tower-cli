/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.responses.computeenvs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
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
