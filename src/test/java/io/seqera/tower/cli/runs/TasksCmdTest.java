/*
 * Copyright 2023, Seqera.
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
