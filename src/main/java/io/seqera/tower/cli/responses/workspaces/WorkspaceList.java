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

package io.seqera.tower.cli.responses.workspaces;

import java.io.PrintWriter;
import java.util.List;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;

public class WorkspaceList extends Response {

    public final String userName;
    public final List<OrgAndWorkspaceDbDto> workspaces;

    public WorkspaceList(String userName, List<OrgAndWorkspaceDbDto> workspaces) {
        this.userName = userName;
        this.workspaces = workspaces;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Workspaces for %s user:|@%n", userName)));

        if (workspaces.isEmpty()) {
            out.println(ansi("    @|yellow No workspaces found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "Workspace ID", "Workspace Name", "Organization Name", "Organization ID").sortBy(0);
        table.setPrefix("    ");
        workspaces.forEach(element -> {
            if(element.getWorkspaceId() != null) {
                table.addRow(element.getWorkspaceId().toString(), element.getWorkspaceName(), element.getOrgName(), element.getOrgId().toString());
            }
        });

        table.print();
        out.println("");
    }
}
