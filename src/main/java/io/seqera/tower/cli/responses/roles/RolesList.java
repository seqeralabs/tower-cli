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

package io.seqera.tower.cli.responses.roles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListRolesResponseRoleInfo;
import jakarta.annotation.Nullable;

import java.io.PrintWriter;
import java.util.List;

public class RolesList extends Response {

    public final String organizationName;
    public final List<ListRolesResponseRoleInfo> roles;

    @JsonIgnore
    @Nullable
    private final PaginationInfo paginationInfo;

    public RolesList(String organizationName, List<ListRolesResponseRoleInfo> roles, @Nullable PaginationInfo paginationInfo) {
        this.organizationName = organizationName;
        this.roles = roles;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Roles for %s organization:|@%n", organizationName)));

        if (roles == null || roles.isEmpty()) {
            out.println(ansi("    @|yellow No roles found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "Name", "Type", "Description");
        table.setPrefix("    ");
        roles.forEach(role -> table.addRow(
                role.getName(),
                Boolean.TRUE.equals(role.getIsPredefined()) ? "predefined" : "custom",
                role.getDescription()
        ));
        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
