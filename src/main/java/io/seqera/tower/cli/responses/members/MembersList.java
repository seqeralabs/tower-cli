package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.MemberDbDto;

import java.io.PrintWriter;
import java.util.List;

public class MembersList extends Response {

    public final String orgName;
    public final List<MemberDbDto> members;

    public MembersList(String orgName, List<MemberDbDto> members) {
        this.orgName = orgName;
        this.members = members;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Members for %s organization:|@%n", orgName)));

        if (members.isEmpty()) {
            out.println(ansi("    @|yellow No members found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Username", "Email", "Role").sortBy(0);
        table.setPrefix("    ");
        members.forEach(element -> {
            table.addRow(element.getMemberId().toString(), element.getUserName(), element.getEmail(), element.getRole().toString());
        });

        table.print();
        out.println("");

    }
}
