package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrganizationDbDto;

import java.io.PrintWriter;

public class OrganizationsView extends Response {

    public final OrganizationDbDto organization;

    public OrganizationsView(OrganizationDbDto organization) {
        this.organization = organization;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Details for organization '%s'|@%n", organization.getFullName())));

        TableList table = new TableList(out, 2);
        table.setPrefix("   ");
        table.addRow("ID", organization.getOrgId().toString());
        table.addRow("Name", organization.getName());
        table.addRow("Full Name", organization.getFullName());
        table.addRow("Description", organization.getWebsite());
        table.addRow("Website", organization.getWebsite());
        table.print();
        out.println("");
    }
}
