/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.responses.workspaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrgAndWorkspaceDto;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatOrgId;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkspaceId;

public class WorkspaceList extends Response {

    public final String userName;
    public final List<OrgAndWorkspaceDto> workspaces;

    @JsonIgnore
    private String serverUrl;

    public WorkspaceList(String userName, List<OrgAndWorkspaceDto> workspaces, String serverUrl) {
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
