package io.seqera.tower.cli.responses.organizations;

import java.io.PrintWriter;
import java.util.List;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;

public class OrganizationsList extends Response {

    public final String userName;
    public final List<OrgAndWorkspaceDbDto> organizations;

    public OrganizationsList(String userName, List<OrgAndWorkspaceDbDto> organizations) {
        this.userName = userName;
        this.organizations = organizations;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Organizations for %s user:|@%n", userName)));

        if (organizations.isEmpty()) {
            out.println(ansi("    @|yellow No Organizations found|@"));
            return;
        }

        TableList table = new TableList(out, 2, "ID", "Name").sortBy(0);
        table.setPrefix("    ");
        organizations.forEach(element -> {
            if (element.getOrgId() != null) {
                table.addRow(element.getOrgId().toString(), element.getOrgName());
            }
        });

        table.print();
        out.println("");
    }
}
