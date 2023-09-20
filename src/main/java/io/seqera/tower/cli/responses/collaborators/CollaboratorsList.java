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

package io.seqera.tower.cli.responses.collaborators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.MemberDbDto;

import javax.annotation.Nullable;
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
