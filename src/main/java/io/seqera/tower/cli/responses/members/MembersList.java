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
