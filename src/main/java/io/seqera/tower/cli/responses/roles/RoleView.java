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

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.RoleDto;

import java.io.PrintWriter;

public class RoleView extends Response {

    public final String organizationName;
    public final RoleDto role;

    public RoleView(String organizationName, RoleDto role) {
        this.organizationName = organizationName;
        this.role = role;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Role '%s' at %s organization:|@%n", role.getName(), organizationName)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("Name", role.getName());
        table.addRow("Type", Boolean.TRUE.equals(role.getIsPredefined()) ? "predefined" : "custom");
        table.addRow("Description", role.getDescription());
        table.print();

        out.println(ansi(String.format("%n  @|bold Permissions:|@%n")));
        if (role.getPermissions() == null || role.getPermissions().isEmpty()) {
            out.println(ansi("    @|yellow No permissions found|@"));
            return;
        }
        role.getPermissions().forEach(permission -> out.println("    " + permission));
        out.println("");
    }
}
