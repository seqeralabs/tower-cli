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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatOrgId;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkspaceId;

public class WorkspaceList extends Response {

    public final String userName;
    public final List<OrgAndWorkspaceDbDto> workspaces;

    @JsonIgnore
    private String serverUrl;

    public WorkspaceList(String userName, List<OrgAndWorkspaceDbDto> workspaces, String serverUrl) {
        this.userName = userName;
        this.workspaces = workspaces;
        this.serverUrl = serverUrl;
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
                table.addRow(
                        formatWorkspaceId(element.getWorkspaceId(), serverUrl, element.getOrgName(), element.getWorkspaceName()),
                        element.getWorkspaceName(),
                        element.getOrgName(),
                        formatOrgId(element.getOrgId(), serverUrl, element.getOrgName())
                );
            }
        });

        table.print();
        out.println("");
    }
}
