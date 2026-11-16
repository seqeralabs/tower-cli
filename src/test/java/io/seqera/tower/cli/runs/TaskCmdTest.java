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
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.runs.tasks.TaskCmd;
import io.seqera.tower.cli.responses.runs.tasks.TaskView;
import io.seqera.tower.model.Task;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void testTaskDetailShowsSpotInterruptions(MockServerClient mock) {
        mockTaskDetail(mock, "\"numSpotInterruptions\": 2,");

        ExecOut out = exec(mock, "runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertTrue(Pattern.compile("Spot interruptions\\s*\\|\\s*2\\s").matcher(out.stdOut).find(), out.stdOut);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"numSpotInterruptions\": 0,", ""})
    void testTaskDetailHidesSpotInterruptionsWhenNone(String extraFields, MockServerClient mock) {
        mockTaskDetail(mock, extraFields);

        ExecOut out = exec(mock, "runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals(0, out.exitCode);
        assertFalse(out.stdOut.contains("Spot interruptions"), out.stdOut);
    }

    @Test
    void testTaskDetailJsonKeepsSpotInterruptionsZero(MockServerClient mock) throws JsonProcessingException {
        mockTaskDetail(mock, "\"numSpotInterruptions\": 0,");

        ExecOut out = exec(OutputType.json, mock, "runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals(0, out.exitCode);
        Map<?, ?> general = (Map<?, ?>) parseJson(out.stdOut, Map.class).get("general");
        assertEquals(0, general.get("numSpotInterruptions"));
    }

    // A Scheduler reason as Platform dev returned it for an Intelligent Compute run
    private static final String SCHED_REASON = "No instance type can fit cpus=1024.0, memory=1792.0GB, gpus=0: region eu-west-1, catalog 621 -> runnable 611 -> machineTypes [c5ad, c5d, c6id, c8id, dl1, f1, f2, g4ad, g4dn, g5, g6, g6e, g6f, g7, g7e, gr6, i3, i3en, i4i, i7i, i7ie, m5ad, m5d, m5dn, m6id, m6idn, m8id, m8idb, m8idn, p3dn, p4d, p4de, p5, p5e, p5en, r5ad, r5d, r5dn, r6id, r6idn, r8id, r8idb, r8idn, x2idn, x2iedn, x8aedz, z1d, c6gd, c7gd, c8gd, c9gd, i8g, i8ge, im4gn, is4gen, m6gd, m7gd, m8gd, m9gd, r6gd, r7gd, r8gd, r9gd, x2gd] 224 -> local-ssd 224";

    @Test
    void testTaskDetailShowsFailureReason(MockServerClient mock) {
        mockTaskDetail(mock, "FAILED", "\"errorMessage\": \"" + SCHED_REASON + "\",");

        ExecOut out = exec(mock, "runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertTrue(out.stdOut.contains("Failure reason"), out.stdOut);
        // On a line of its own, not in the General table
        assertTrue(out.stdOut.lines().anyMatch(line -> line.equals("     " + SCHED_REASON)), out.stdOut);
    }

    @Test
    void testTaskDetailLabelsAbortedTaskReason(MockServerClient mock) {
        mockTaskDetail(mock, "ABORTED", "\"errorMessage\": \"Stopped because the run was cancelled.\",");

        ExecOut out = exec(mock, "runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals(0, out.exitCode);
        assertFalse(out.stdOut.contains("Failure reason"), out.stdOut);
        assertTrue(out.stdOut.contains("Reason"), out.stdOut);
        assertTrue(out.stdOut.lines().anyMatch(line -> line.equals("     Stopped because the run was cancelled.")), out.stdOut);
    }

    @Test
    void testTaskDetailHidesReasonWhenNone(MockServerClient mock) {
        mockTaskDetail(mock, "FAILED", "");

        ExecOut out = exec(mock, "runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals(0, out.exitCode);
        assertFalse(out.stdOut.toLowerCase().contains("reason"), out.stdOut);
    }

    @Test
    void testTaskDetailJsonIncludesFailureReason(MockServerClient mock) throws JsonProcessingException {
        mockTaskDetail(mock, "FAILED", "\"errorMessage\": \"" + SCHED_REASON + "\",");

        ExecOut out = exec(OutputType.json, mock, "runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1");

        assertEquals(0, out.exitCode);
        Map<?, ?> general = (Map<?, ?>) parseJson(out.stdOut, Map.class).get("general");
        assertEquals(SCHED_REASON, general.get("errorMessage"));
    }

    private void mockTaskDetail(MockServerClient mock, String extraFields) {
        mockTaskDetail(mock, "COMPLETED", extraFields);
    }

    private void mockTaskDetail(MockServerClient mock, String status, String extraFields) {
        String body = new String(loadResource("runs/task_detail"), StandardCharsets.UTF_8)
                .replaceFirst("\"status\": \"COMPLETED\"", "\"status\": \"" + status + "\"")
                .replaceFirst("\"task\":\\s*\\{", "\"task\": {" + extraFields);

        mock.when(
                request().withMethod("GET").withPath("/workflow/5J9pBnWd6uoC3w/task/1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(body).withContentType(MediaType.APPLICATION_JSON)
        );
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
