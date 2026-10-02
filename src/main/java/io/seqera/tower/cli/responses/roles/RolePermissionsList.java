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
import io.seqera.tower.model.RolePermissionResponseDto;

import java.io.PrintWriter;
import java.util.List;

public class RolePermissionsList extends Response {

    public final List<RolePermissionResponseDto> permissions;

    public RolePermissionsList(List<RolePermissionResponseDto> permissions) {
        this.permissions = permissions;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Role permissions:|@%n")));

        if (permissions == null || permissions.isEmpty()) {
            out.println(ansi("    @|yellow No permissions found|@"));
            return;
        }

        TableList table = new TableList(out, 2, "Category", "Name");
        table.setPrefix("    ");
        permissions.forEach(permission -> table.addRow(permission.getCategory(), permission.getName()));
        table.print();
        out.println("");
    }
}
