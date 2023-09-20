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

package io.seqera.tower.cli.runs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.runs.tasks.enums.TaskColumn;
import io.seqera.tower.cli.responses.runs.tasks.TasksView;
import io.seqera.tower.model.Task;
import io.seqera.tower.model.TaskStatus;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class TasksCmdTest extends BaseCmdTest {

    @Test
    void listRunTasksTests(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/2zGxKoqlnVmGL/tasks"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/tasks_list_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "view", "-i", "2zGxKoqlnVmGL", "tasks");

        List<TaskColumn> cols = Stream.of(TaskColumn.values())
                .filter(TaskColumn::isFixed)
                .collect(Collectors.toList());

        List<Task> tasks = List.of(
                new Task().taskId(1L)
                        .process("NFCORE_RNASEQ:RNASEQ:PREPARE_GENOME:GUNZIP_ADDITIONAL_FASTA")
                        .tag("gfp.fa.gz")
                        .status(TaskStatus.COMPLETED),
                new Task().taskId(2L)
                        .process("NFCORE_RNASEQ:RNASEQ:PREPARE_GENOME:UNTAR_STAR_INDEX")
                        .status(TaskStatus.COMPLETED)
        );

        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new TasksView("2zGxKoqlnVmGL", cols, tasks, null).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
