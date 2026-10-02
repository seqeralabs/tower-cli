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

package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListUserRolesResponse;
import io.seqera.tower.model.UserWorkspaceRoleDto;
import io.seqera.tower.model.WspRoleSourceType;

import java.io.PrintWriter;

public class MemberRolesList extends Response {

    public final String organizationName;
    public final ListUserRolesResponse userRoles;

    public MemberRolesList(String organizationName, ListUserRolesResponse userRoles) {
        this.organizationName = organizationName;
        this.userRoles = userRoles;
    }

    @Override
    public Object getJSON() {
        return userRoles;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Workspace roles of '%s' in %s organization:|@%n", userRoles.getUser().getUserName(), organizationName)));

        if (userRoles.getUserWorkspaces() == null || userRoles.getUserWorkspaces().isEmpty()) {
            out.println(ansi("    @|yellow No workspace roles found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "Workspace ID", "Workspace Name", "Role", "Granted via");
        table.setPrefix("    ");
        userRoles.getUserWorkspaces().forEach(ws -> ws.getRoles().forEach(role ->
                table.addRow(ws.getWorkspaceId().toString(), ws.getWorkspaceName(), role.getRole(), grantedVia(role))
        ));
        table.print();
        out.println("");
    }

    private static String grantedVia(UserWorkspaceRoleDto role) {
        return role.getRoleSourceType() == WspRoleSourceType.team ? "team " + role.getSourceTeamName() : "direct";
    }
}
