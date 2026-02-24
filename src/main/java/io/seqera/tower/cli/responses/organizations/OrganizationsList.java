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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.OrgAndWorkspaceDto;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatOrgId;

public class OrganizationsList extends Response {

    public final String userName;
    public final List<OrgAndWorkspaceDto> organizations;

    @JsonIgnore
    private final String serverUrl;

    public OrganizationsList(String userName, List<OrgAndWorkspaceDto> organizations, String serverUrl) {
        this.userName = userName;
        this.organizations = organizations;
        this.serverUrl = serverUrl;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Organizations for %s user:|@%n", userName)));

        if (organizations.isEmpty()) {
            out.println(ansi("    @|yellow No organizations found|@"));
            return;
        }

        TableList table = new TableList(out, 2, "ID", "Name").sortBy(0);
        table.setPrefix("    ");
        organizations.forEach(element -> {
            if (element.getOrgId() != null) {
                table.addRow(formatOrgId(element.getOrgId(), serverUrl, element.getOrgName()), element.getOrgName());
            }
        });

        table.print();
        out.println("");
    }
}
