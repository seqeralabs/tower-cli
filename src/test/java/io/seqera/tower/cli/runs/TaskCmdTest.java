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
import io.seqera.tower.cli.commands.runs.tasks.TaskCmd;
import io.seqera.tower.cli.responses.TaskView;
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

        ExecOut out = exec(mock, "-v","runs", "view", "-i", "5J9pBnWd6uoC3w", "task", "-t", "1", "--execution-time", "--resources-requested", "--resources-usage");

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
}
