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

package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrganizationQuotas;

import java.io.PrintWriter;
import java.util.Objects;

public class OrganizationQuotasView extends Response {

    public final String organizationName;
    public final OrganizationQuotas quotas;

    public OrganizationQuotasView(String organizationName, OrganizationQuotas quotas) {
        this.organizationName = organizationName;
        this.quotas = quotas;
    }

    @Override
    public Object getJSON() {
        return quotas;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Quotas for %s organization:|@%n", organizationName)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("Workspaces", Objects.toString(quotas.getMaxWorkspaces(), null));
        table.addRow("Members", Objects.toString(quotas.getMaxMembers(), null));
        table.addRow("Teams", Objects.toString(quotas.getMaxTeams(), null));
        table.addRow("Custom roles", Objects.toString(quotas.getMaxCustomRolesPerOrg(), null));
        table.addRow("Participants per workspace", Objects.toString(quotas.getMaxParticipantsPerWorkspace(), null));
        table.addRow("Pipelines per workspace", Objects.toString(quotas.getMaxPipelinesPerWorkspace(), null));
        table.addRow("Datasets per workspace", Objects.toString(quotas.getMaxDatasetsPerWorkspace(), null));
        table.addRow("Versions per dataset", Objects.toString(quotas.getMaxVersionsPerDataset(), null));
        table.addRow("Labels per workspace", Objects.toString(quotas.getMaxLabelsPerWorkspace(), null));
        table.addRow("Runs", Objects.toString(quotas.getMaxRuns(), null));
        table.addRow("Run history", Objects.toString(quotas.getMaxRunHistory(), null));
        table.addRow("Running Studios", Objects.toString(quotas.getMaxDataStudiosRunning(), null));
        table.addRow("Seqera Compute environments", Objects.toString(quotas.getMaxSeqeraComputeComputeEnvs(), null));
        table.addRow("Fusion throughput (bytes)", Objects.toString(quotas.getMaxFusionThroughputBytes(), null));
        table.print();
        out.println("");
    }
}
