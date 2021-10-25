package io.seqera.tower.cli.responses.teams.members;

import java.io.PrintWriter;
import java.util.List;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.MemberDbDto;

public class TeamMembersList extends Response {
    Long teamId;
    List<MemberDbDto> members;

    public TeamMembersList(Long teamId, List<MemberDbDto> members) {
        this.teamId = teamId;
        this.members = members;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Members for team '%d':|@%n", teamId)));

        if (members.isEmpty()) {
            out.println(ansi("    @|yellow No team members found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "Member ID", "Username", "Email", "Role").sortBy(0);
        table.setPrefix("    ");
        members.forEach(element -> {
            table.addRow(element.getMemberId().toString(), element.getUserName(), element.getEmail(), element.getRole().toString());
        });

        table.print();
        out.println("");
    }
}
