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

package io.seqera.tower.cli.responses.collaborators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.MemberDbDto;

import jakarta.annotation.Nullable;
import java.io.PrintWriter;
import java.util.List;

public class CollaboratorsList extends Response {

    public final Long organizationId;
    public final List<MemberDbDto> members;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public CollaboratorsList(Long organizationId, List<MemberDbDto> members, @Nullable PaginationInfo paginationInfo) {
        this.organizationId = organizationId;
        this.members = members;
        this.paginationInfo = paginationInfo;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Collaborators for %d organization:|@%n", organizationId)));

        if (members.isEmpty()) {
            out.println(ansi("    @|yellow No collaborators found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "ID", "Username", "Email");
        table.setPrefix("    ");
        members.forEach(element -> {
            table.addRow(element.getMemberId().toString(), element.getUserName(), element.getEmail());
        });

        table.print();

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
