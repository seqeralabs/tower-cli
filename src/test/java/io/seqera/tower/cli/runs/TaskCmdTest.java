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

package io.seqera.tower.cli.runs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.runs.tasks.TaskCmd;
import io.seqera.tower.cli.responses.runs.tasks.TaskView;
import io.seqera.tower.model.Task;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Map;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TaskCmdTest extends BaseCmdTest {

    @Test
    void testTaskDetail(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5J9pBnWd6uoC3w/task/1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/task_detail")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock,"runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1", "--execution-time", "--resources-requested", "--resources-usage");

        Task task = parseJson(new String(loadResource("runs/task_object")), Task.class);

        Map<String, Object> general = TaskCmd.parseGeneralData(task);
        String command = task.getScript() != null ? task.getScript() : null;
        String environment = task.getEnv() != null ? task.getEnv() : null;
        Map<String, Object> times = TaskCmd.parseExecutionTimeData(task);
        Map<String, Object> resources = TaskCmd.parseResourcesRequestedData(task);
        Map<String, Object> usage = TaskCmd.parseResourcesUsageData(task);

        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new TaskView(general, command, environment, times, resources, usage).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testTaskDetailWithNullValues(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5J9pBnWd6uoC3w/task/1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/task_detail_null_values")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock,"runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1", "--execution-time", "--resources-requested", "--resources-usage");

        Task task = parseJson(new String(loadResource("runs/task_object_null_values")), Task.class);

        Map<String, Object> general = TaskCmd.parseGeneralData(task);
        String command = task.getScript() != null ? task.getScript() : null;
        String environment = task.getEnv() != null ? task.getEnv() : null;
        Map<String, Object> times = TaskCmd.parseExecutionTimeData(task);
        Map<String, Object> resources = TaskCmd.parseResourcesRequestedData(task);
        Map<String, Object> usage = TaskCmd.parseResourcesUsageData(task);

        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new TaskView(general, command, environment, times, resources, usage).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    // FIXME: Workaround for Platform versions before 26.x returning exit as String. Remove once those versions are phased out (see #578).
    // Verifies that the task view command does not fail when the API returns the exit code as a string ("0") instead of an integer (0).
    // The deserialization logic is tested in detail by TaskExitMixinTest.
    @Test
    void testTaskDetailWithExitAsString(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5J9pBnWd6uoC3w/task/1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/task_detail_exit_as_string")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock,"runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }
}
