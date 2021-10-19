package io.seqera.tower.cli.responses.teams;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.TeamDbDto;

import java.io.PrintWriter;
import java.util.List;

public class TeamsList extends Response {

    public final String organizationName;
    public final List<TeamDbDto> teams;

    public TeamsList(String organizationName, List<TeamDbDto> teams) {
        this.organizationName = organizationName;
        this.teams = teams;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Teams for %s organization:|@%n", organizationName)));

        if (teams.isEmpty()) {
            out.println(ansi("    @|yellow No teams found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "Team ID", "Team Name", "Members Count Name").sortBy(0);
        table.setPrefix("    ");
        teams.forEach(element -> {
            table.addRow(element.getTeamId().toString(), element.getName(), element.getMembersCount().toString());
        });

        table.print();
        out.println("");

    }
}
