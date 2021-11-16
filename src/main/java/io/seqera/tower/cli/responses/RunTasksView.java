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

package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;

import java.io.PrintWriter;
import java.util.List;

public class RunTasksView extends Response {

    public final String runId;
    public final List<String> columns;
    public final List<List<String>> tasks;

    public RunTasksView(String runId, List<String> columns, List<List<String>> tasks) {
        this.runId = runId;
        this.columns = columns;
        this.tasks = tasks;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Pipeline's run %s tasks:|@%n", runId)));

        TableList table = new TableList(out, columns.size(), columns.toArray(new String[0])).sortBy(0);
        table.setPrefix("    ");
        tasks.forEach(it -> {
            table.addRow(it.toArray(new String[0]));
        });
        table.print();
        out.println("");
    }
}
