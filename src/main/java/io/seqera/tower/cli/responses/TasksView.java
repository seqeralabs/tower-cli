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

import io.seqera.tower.cli.commands.runs.tasks.enums.TaskColumn;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DescribeTaskResponse;
import io.seqera.tower.model.ListTasksResponse;
import io.seqera.tower.model.Task;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TasksView extends Response {

    public final String runId;
    public final List<TaskColumn> columns;
    public final List<Task> tasks;

    public TasksView(String runId, List<TaskColumn> columns, List<Task> tasks) {
        this.runId = runId;
        this.columns = columns;
        this.tasks = tasks;
    }

    @Override
    public Object getJSON() {
        return Objects.requireNonNull(tasks).stream().map(
                task -> columns.stream()
                    .filter(col -> col.getObject().apply(task) != null)
                    .collect(
                            Collectors.toUnmodifiableMap(TaskColumn::name, col -> col.getObject().apply(task))
                    )
        ).collect(Collectors.toList());
    }

    @Override
    public void toString(PrintWriter out) {
        List<String> cols = columns.stream().map(TaskColumn::getDescription).collect(Collectors.toList());
        List<List<String>> result = new ArrayList<>();

        Objects.requireNonNull(tasks).forEach(task -> {
            List<String> items = columns.stream()
                    .map(colItem -> colItem.getObject().apply(task) != null ? colItem.getPrettyPrint().apply(task) : null)
                    .collect(Collectors.toList());
            result.add(items);
        });

        out.println(ansi(String.format("%n  @|bold Pipeline's run %s tasks:|@%n", runId)));

        TableList table = new TableList(out, cols.size(), cols.toArray(new String[0]));
        table.setPrefix("    ");
        result.forEach(it -> {
            table.addRow(it.toArray(new String[0]));
        });
        table.print();
        out.println("");
    }
}
