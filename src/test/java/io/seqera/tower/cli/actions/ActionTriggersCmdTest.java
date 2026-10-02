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

package io.seqera.tower.cli.actions;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.actions.ActionTriggerView;
import io.seqera.tower.cli.responses.actions.ActionTriggersList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ActionTriggerDto;
import io.seqera.tower.model.ActionTriggerResponseDto;
import io.seqera.tower.model.ActionTriggerResult;
import io.seqera.tower.model.ActionSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.time.OffsetDateTime;
import java.util.List;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class ActionTriggersCmdTest extends BaseCmdTest {

    private static final String ACTION_ID = "57byWxhmUDLLWIF4J97XEP";

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) {
        mock.reset();
        mockUserInfo(mock);

        mock.when(
                request().withMethod("GET").withPath("/actions/" + ACTION_ID + "/triggers")
                        .withQueryStringParameter("max", "10")
                        .withQueryStringParameter("offset", "10")
                        .withQueryStringParameter("outcome", "LAUNCHED", "LAUNCH_FAILED"),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"totalSize\": 11, \"triggers\": [{\"id\": \"t1\", \"actionId\": \"" + ACTION_ID + "\", \"source\": \"cron\", " +
                        "\"outcome\": \"LAUNCHED\", \"firedAt\": \"2026-09-30T02:00:00Z\", \"eventSummary\": \"Scheduled run\", \"actorId\": 1, \"workflowId\": \"wf1\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "actions", "triggers", "list", "-i", ACTION_ID, "--outcome", "LAUNCHED,LAUNCH_FAILED", "--max", "10", "--page", "2");

        ActionTriggerDto trigger = new ActionTriggerDto().id("t1").actionId(ACTION_ID).source(ActionSource.cron).outcome(ActionTriggerResult.LAUNCHED)
                .firedAt(OffsetDateTime.parse("2026-09-30T02:00:00Z")).eventSummary("Scheduled run").actorId(1L).workflowId("wf1");
        assertOutput(format, out, new ActionTriggersList(ACTION_ID, USER_WORKSPACE_NAME, List.of(trigger), baseUserUrl(mock, USER_WORKSPACE_NAME), PaginationInfo.from(null, 10, 2, 11L)));
    }

    @Test
    void testListByActionName(MockServerClient mock) {
        mock.reset();
        mockUserInfo(mock);

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/" + ACTION_ID + "/triggers"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"totalSize\": 0, \"triggers\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "triggers", "list", "-n", "hello");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertTrue(out.stdOut.contains("No triggers found"), out.stdOut);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) {
        mock.reset();
        mockUserInfo(mock);

        mock.when(
                request().withMethod("GET").withPath("/actions/" + ACTION_ID + "/triggers/t1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"trigger\": {\"id\": \"t1\", \"actionId\": \"" + ACTION_ID + "\", \"source\": \"pipeline_status\", " +
                        "\"outcome\": \"LAUNCH_FAILED\", \"outcomeDetail\": \"Compute environment not available\", \"firedAt\": \"2026-09-30T02:00:00Z\", " +
                        "\"eventSummary\": \"Run wf0 FAILED\", \"eventPayload\": \"{\\\"runStatus\\\": \\\"FAILED\\\"}\", \"actorId\": 1, \"causedByTriggerId\": \"t0\"}}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "actions", "triggers", "view", "-i", ACTION_ID, "--trigger-id", "t1");

        ActionTriggerResponseDto trigger = new ActionTriggerResponseDto().id("t1").actionId(ACTION_ID).source(ActionSource.pipeline_status)
                .outcome(ActionTriggerResult.LAUNCH_FAILED).outcomeDetail("Compute environment not available").firedAt(OffsetDateTime.parse("2026-09-30T02:00:00Z"))
                .eventSummary("Run wf0 FAILED").eventPayload("{\"runStatus\": \"FAILED\"}").actorId(1L).causedByTriggerId("t0");
        assertOutput(format, out, new ActionTriggerView(trigger, baseUserUrl(mock, USER_WORKSPACE_NAME)));
    }

    @Test
    void testViewStripsControlCharactersFromEvent(MockServerClient mock) {
        mock.reset();
        mockUserInfo(mock);

        // The payload carries an ANSI escape sequence (ESC [31m) that would recolour the terminal
        mock.when(
                request().withMethod("GET").withPath("/actions/" + ACTION_ID + "/triggers/t1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"trigger\": {\"id\": \"t1\", \"actionId\": \"" + ACTION_ID + "\", \"outcome\": \"LAUNCHED\", " +
                        "\"eventSummary\": \"push \\u001b[31mred\", \"eventPayload\": \"line1\\nline2 \\u001b]8;;http://evil\\u0007\"}}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "triggers", "view", "-i", ACTION_ID, "--trigger-id", "t1");

        assertEquals(0, out.exitCode);
        assertFalse(out.stdOut.contains("\u001b"), out.stdOut);
        assertFalse(out.stdOut.contains("\u0007"), out.stdOut);
        assertTrue(out.stdOut.contains("push [31mred"), out.stdOut);
        assertTrue(out.stdOut.contains("line1\n     line2 ]8;;http://evil"), out.stdOut);
    }

    @Test
    void testViewNotFound(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions/" + ACTION_ID + "/triggers/missing"), exactly(1)
        ).respond(
                response().withStatusCode(404).withBody("{\"message\": \"Unknown action trigger id: missing\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "triggers", "view", "-i", ACTION_ID, "--trigger-id", "missing");

        assertEquals(1, out.exitCode);
        assertTrue(out.stdErr.contains("Unknown action trigger id: missing"), out.stdErr);
    }

    private void mockUserInfo(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info")
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );
    }
}
