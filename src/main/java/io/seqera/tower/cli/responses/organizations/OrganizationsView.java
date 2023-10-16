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

package io.seqera.tower.cli.responses.organizations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrganizationDbDto;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatOrgId;

public class OrganizationsView extends Response {

    public final OrganizationDbDto organization;

    @JsonIgnore
    private final String serverUrl;

    public OrganizationsView(OrganizationDbDto organization, String serverUrl) {
        this.organization = organization;
        this.serverUrl = serverUrl;
    }

    @Override
    public Object getJSON() {
        return organization;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Details for organization '%s'|@%n", organization.getFullName())));

        TableList table = new TableList(out, 2);
        table.setPrefix("   ");
        table.addRow("ID", formatOrgId(organization.getOrgId(), serverUrl, organization.getName()));
        table.addRow("Name", organization.getName());
        table.addRow("Full Name", organization.getFullName());
        table.addRow("Description", organization.getDescription());
        table.addRow("Website", organization.getWebsite());
        table.print();
        out.println("");
    }
}
