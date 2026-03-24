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

package io.seqera.tower.cli.responses.runs.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.seqera.tower.cli.commands.runs.tasks.enums.TaskColumn;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Task;

import jakarta.annotation.Nullable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TasksView extends Response {

    public final String runId;
    public final List<TaskColumn> columns;
    public final List<Task> tasks;

    @JsonIgnore
    @Nullable
    private PaginationInfo paginationInfo;

    public TasksView(String runId, List<TaskColumn> columns, List<Task> tasks, @Nullable PaginationInfo paginationInfo) {
        this.runId = runId;
        this.columns = columns;
        this.tasks = tasks;
        this.paginationInfo = paginationInfo;
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

        PaginationInfo.addFooter(out, paginationInfo);

        out.println("");
    }
}
