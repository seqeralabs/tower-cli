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

package io.seqera.tower.cli.commands.runs.tasks;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.commands.runs.tasks.enums.TaskColumn;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.tasks.TasksView;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.DescribeTaskResponse;
import io.seqera.tower.model.ListTasksResponse;
import io.seqera.tower.model.Task;
import picocli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "tasks",
        description = "Display pipeline run tasks"
)
public class TasksCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-c", "--columns"}, split = ",", description = "Additional task columns to display beyond the default set. Available columns: ${COMPLETION-CANDIDATES}. Comma-separated list.")
    public List<TaskColumn> columns;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Filter tasks by name prefix. Shows only tasks with names starting with the specified string.")
    public String startsWith;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @CommandLine.ParentCommand
    public ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(parentCommand.workspace.workspace);

        List<TaskColumn> cols = Stream.of(TaskColumn.values())
                .filter(it -> it.isFixed() || (columns != null && columns.contains(it)))
                .collect(Collectors.toList());

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListTasksResponse response = workflowsApi().listWorkflowTasks(parentCommand.id, wspId, max, offset, null, null, startsWith);
        List<Task> tasks = new ArrayList<>();
        for (DescribeTaskResponse describeTaskResponse : Objects.requireNonNull(response.getTasks())) {
            Task task = describeTaskResponse.getTask();
            tasks.add(task);
        }
        return new TasksView(parentCommand.id, cols, tasks, PaginationInfo.from(paginationOptions));
    }
}
