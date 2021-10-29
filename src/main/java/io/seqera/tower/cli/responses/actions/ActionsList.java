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

package io.seqera.tower.cli.responses.actions;

import java.io.PrintWriter;
import java.util.List;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListActionsResponseActionInfo;

public class ActionsList extends Response {

    public final String userName;
    public final List<ListActionsResponseActionInfo> actions;

    public ActionsList(List<ListActionsResponseActionInfo> actions, String userName) {
        this.userName = userName;
        this.actions = actions;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Actions for %s user:|@%n", userName)));

        if (actions.isEmpty()) {
            out.println(ansi("    @|yellow No actions found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "ID", "Name", "Endpoint", "Status", "Source").sortBy(0);
        table.setPrefix("    ");
        actions.forEach(element -> {
            table.addRow(element.getId(), element.getName(), element.getEndpoint(), element.getStatus().toString(), element.getSource().toString());
        });

        table.print();
        out.println("");
    }
}
