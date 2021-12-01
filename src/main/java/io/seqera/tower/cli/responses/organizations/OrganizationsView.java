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
