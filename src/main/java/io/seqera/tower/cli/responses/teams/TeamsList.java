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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.TeamDbDto;

import jakarta.annotation.Nullable;
import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatTeamId;

public class TeamsList extends Response {

    public final String organizationName;
    public final List<TeamDbDto> teams;

    @JsonIgnore
    private String baseOrgUrl;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public TeamsList(String organizationName, List<TeamDbDto> teams, String baseOrgUrl, @Nullable PaginationInfo paginationInfo) {
        this.organizationName = organizationName;
        this.teams = teams;
        this.baseOrgUrl = baseOrgUrl;
        this.paginationInfo = paginationInfo;
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

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");

    }
}
