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

package io.seqera.tower.cli.responses.teams.members;

import java.io.PrintWriter;
import java.util.List;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.MemberDbDto;

public class TeamMembersList extends Response {
    public final String teamName;
    public final List<MemberDbDto> members;

    public TeamMembersList(String teamName, List<MemberDbDto> members) {
        this.teamName = teamName;
        this.members = members;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Members for team '%s':|@%n", teamName)));

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
