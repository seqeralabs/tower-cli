/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.responses.teams;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.TeamDbDto;

import java.io.PrintWriter;

public class TeamView extends Response {

    public final String organizationName;
    public final TeamDbDto team;

    public TeamView(String organizationName, TeamDbDto team) {
        this.organizationName = organizationName;
        this.team = team;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Team '%s' at %s organization:|@%n", team.getName(), organizationName)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", team.getTeamId().toString());
        table.addRow("Name", team.getName());
        table.addRow("Description", team.getDescription());
        table.addRow("Members", team.getMembersCount() == null ? null : team.getMembersCount().toString());
        table.addRow("IdP group", team.getIdpGroupName());
        table.print();
        out.println("");
    }
}
