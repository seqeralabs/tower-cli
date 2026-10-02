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
import io.seqera.tower.model.WorkspaceParticipantResponseDto;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.List;

public class TeamWorkspacesList extends Response {

    public final String organizationName;
    public final String teamName;
    public final List<WorkspaceParticipantResponseDto> workspaces;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public TeamWorkspacesList(String organizationName, String teamName, List<WorkspaceParticipantResponseDto> workspaces, @Nullable PaginationInfo paginationInfo) {
        this.organizationName = organizationName;
        this.teamName = teamName;
        this.workspaces = workspaces;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Workspaces of team '%s' at %s organization:|@%n", teamName, organizationName)));

        if (workspaces == null || workspaces.isEmpty()) {
            out.println(ansi("    @|yellow No workspaces found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "Workspace ID", "Workspace Name", "Role");
        table.setPrefix("    ");
        workspaces.forEach(ws -> table.addRow(ws.getWorkspaceId().toString(), ws.getWorkspaceName(), ws.getParticipantRole()));
        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
