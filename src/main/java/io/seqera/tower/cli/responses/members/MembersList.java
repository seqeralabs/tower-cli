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

package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.MemberDbDto;

import java.io.PrintWriter;
import java.util.List;

import static io.seqera.tower.cli.utils.FormatHelper.formatOrgRole;

public class MembersList extends Response {

    public final String orgName;
    public final List<MemberDbDto> members;

    public MembersList(String orgName, List<MemberDbDto> members) {
        this.orgName = orgName;
        this.members = members;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Members for %s organization:|@%n", orgName)));

        if (members.isEmpty()) {
            out.println(ansi("    @|yellow No members found|@"));
            return;
        }

        TableList table = new TableList(out, 4, "ID", "Username", "Email", "Role");
        table.setPrefix("    ");
        members.forEach(element -> {
            table.addRow(element.getMemberId().toString(), element.getUserName(), element.getEmail(), formatOrgRole(element.getRole()));
        });

        table.print();
        out.println("");

    }
}
