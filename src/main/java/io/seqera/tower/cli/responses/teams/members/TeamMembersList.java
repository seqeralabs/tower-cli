/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.responses.teams.members;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.MemberDbDto;

import java.io.PrintWriter;
import java.util.List;

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
