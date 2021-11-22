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

package io.seqera.tower.cli.commands.runs.tasks;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.commands.runs.tasks.enums.TaskColumn;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.TasksView;
import io.seqera.tower.model.ListTasksResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "tasks",
        description = "Display pipeline's run tasks."
)
public class TasksCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-c", "--columns"}, split = ",", description = "Additional task columns to display: ${COMPLETION-CANDIDATES}")
    public List<TaskColumn> columns;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Only show task with parameters that start with the given word")
    public String startsWith;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @CommandLine.ParentCommand
    public ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException, IOException {
        List<TaskColumn> cols = Stream.of(TaskColumn.values())
                .filter(it -> it.isFixed() || (columns != null && columns.contains(it)))
                .collect(Collectors.toList());

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListTasksResponse response = api().listWorkflowTasks(parentCommand.id, parentCommand.workspace.workspaceId, max, offset, startsWith);

        return new TasksView(parentCommand.id, cols, response);
    }
}
