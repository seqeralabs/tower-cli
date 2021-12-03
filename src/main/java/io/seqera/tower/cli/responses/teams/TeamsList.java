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

package io.seqera.tower.cli.responses.teams;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.TeamDbDto;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatTeamId;

public class TeamsList extends Response {

    public final String organizationName;
    public final List<TeamDbDto> teams;

    @JsonIgnore
    private String baseOrgUrl;

    public TeamsList(String organizationName, List<TeamDbDto> teams, String baseOrgUrl) {
        this.organizationName = organizationName;
        this.teams = teams;
        this.baseOrgUrl = baseOrgUrl;
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
            table.addRow(formatTeamId(element.getTeamId(), baseOrgUrl), element.getName(), element.getMembersCount().toString());
        });

        table.print();
        out.println("");

    }
}
