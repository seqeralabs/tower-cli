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

package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.IdpGroupEntry;

import java.io.PrintWriter;
import java.util.List;

public class IdpGroupsList extends Response {

    public final String organizationName;
    public final List<IdpGroupEntry> groups;

    public IdpGroupsList(String organizationName, List<IdpGroupEntry> groups) {
        this.organizationName = organizationName;
        this.groups = groups;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold IdP groups for %s organization:|@%n", organizationName)));

        if (groups == null || groups.isEmpty()) {
            out.println(ansi("    @|yellow No IdP groups found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "ID", "Name", "Source");
        table.setPrefix("    ");
        groups.forEach(group -> table.addRow(group.getId().toString(), group.getDisplayName(), group.getSource() == null ? null : group.getSource().toString()));
        table.print();
        out.println("");
    }
}
