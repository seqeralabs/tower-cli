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


package io.seqera.tower.cli.agents;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.agents.AgentAdded;
import io.seqera.tower.cli.responses.agents.AgentDeleted;
import io.seqera.tower.cli.responses.agents.AgentLaunched;
import io.seqera.tower.cli.responses.agents.AgentRunView;
import io.seqera.tower.cli.responses.agents.AgentRunsList;
import io.seqera.tower.cli.responses.agents.AgentUpdated;
import io.seqera.tower.cli.responses.agents.AgentView;
import io.seqera.tower.cli.responses.agents.AgentsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.AgentDbDto;
import io.seqera.tower.model.AgentRunDbDto;
import io.seqera.tower.model.AgentRunStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class AgentsCmdTest extends BaseCmdTest {

    private static final String WSP_ID = "75887156211589";
    private static final String WSP_REF = "[organization1 / workspace1]";

    private static final String AGENT = """
            {
              "id": "agt_1",
              "name": "fix-failed-runs",
              "description": "Debug failed runs",
              "agentInstructions": "Find the root cause",
              "agentInstructionsTemplateId": "debug-template",
              "status": "active",
              "workspaceId": 75887156211589,
              "createdBy": 1264,
              "dateCreated": "2026-09-01T10:00:00Z",
              "lastUpdated": "2026-09-02T10:00:00Z"
            }
            """;

    @BeforeEach
    void init(MockServerClient mock) {
        mock.reset();
        mock.when(request().withMethod("GET").withPath("/user-info"), exactly(1))
                .respond(response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON));
        mock.when(request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1))
                .respond(response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON));
    }

    private void mockDescribe(MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath("/agents/agt_1").withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agent\":" + AGENT + "}").withContentType(MediaType.APPLICATION_JSON));
    }

    private void mockFindByName(MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath("/agents")
                        .withQueryStringParameter("workspaceId", WSP_ID)
                        .withQueryStringParameter("search", "fix-failed-runs")
                        .withQueryStringParameter("max", "100")
                        .withQueryStringParameter("offset", "0"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agents\":[" + AGENT.replace("fix-failed-runs", "fix-failed-runs-v2").replace("agt_1", "agt_2") + "," + AGENT + "],\"totalSize\":2}").withContentType(MediaType.APPLICATION_JSON));
    }

    private static AgentDbDto agent() throws JsonProcessingException {
        return parseJson(AGENT, AgentDbDto.class);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(request().withMethod("GET").withPath("/agents")
                        .withQueryStringParameter("workspaceId", WSP_ID)
                        .withQueryStringParameter("search", "fix")
                        .withQueryStringParameter("max", "10")
                        .withQueryStringParameter("offset", "10"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agents\":[" + AGENT + "],\"totalSize\":11}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "list", "-w", WSP_ID, "-f", "fix", "--max", "10", "--page", "2");

        assertOutput(format, out, new AgentsList(WSP_REF, List.of(agent()), PaginationInfo.from(null, 10, 2, 11L)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testViewByName(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mockFindByName(mock);
        mockDescribe(mock);

        ExecOut out = exec(format, mock, "agents", "view", "-w", WSP_ID, "-n", "fix-failed-runs");

        assertOutput(format, out, new AgentView(WSP_REF, agent()));
    }

    @Test
    void testViewUnknownName(MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath("/agents").withQueryStringParameter("search", "missing"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agents\":[],\"totalSize\":0}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(mock, "agents", "view", "-w", WSP_ID, "-n", "missing");

        assertEquals(errorMessage(out.app, new TowerException("Unknown agent 'missing' at " + WSP_REF + " workspace")), out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws IOException {
        mock.when(request().withMethod("POST").withPath("/agents").withQueryStringParameter("workspaceId", WSP_ID)
                        .withBody(json("""
                                {
                                  "name": "fix-failed-runs",
                                  "description": "Debug failed runs",
                                  "agentInstructions": "Find the root cause"
                                }
                                """)), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agent\":" + AGENT + "}").withContentType(MediaType.APPLICATION_JSON));

        String instructions = tempFile("Find the root cause", "instructions", ".md");
        ExecOut out = exec(format, mock, "agents", "add", "-w", WSP_ID, "-n", "fix-failed-runs", "-d", "Debug failed runs",
                "--instructions-file", instructions);

        assertOutput(format, out, new AgentAdded(WSP_REF, agent()));
    }

    @Test
    void testAddRequiresInstructions(MockServerClient mock) {
        ExecOut out = exec(mock, "agents", "add", "-w", WSP_ID, "-n", "fix-failed-runs");

        assertEquals(2, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdate(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mockDescribe(mock);
        // Fields not given on the command line keep their current value, as the API replaces the agent
        mock.when(request().withMethod("PUT").withPath("/agents/agt_1").withQueryStringParameter("workspaceId", WSP_ID)
                        .withBody(json("""
                                {
                                  "name": "new-name",
                                  "description": "Debug failed runs",
                                  "agentInstructions": "New instructions",
                                  "agentInstructionsTemplateId": "debug-template"
                                }
                                """)), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agent\":" + AGENT.replace("\"fix-failed-runs\"", "\"new-name\"") + "}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "update", "-w", WSP_ID, "-i", "agt_1", "--new-name", "new-name", "--instructions", "New instructions");

        assertOutput(format, out, new AgentUpdated(WSP_REF, "new-name", "updated"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {
        mockDescribe(mock);
        mock.when(request().withMethod("DELETE").withPath("/agents/agt_1").withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(204));

        ExecOut out = exec(format, mock, "agents", "delete", "-w", WSP_ID, "-i", "agt_1");

        assertOutput(format, out, new AgentDeleted(WSP_REF, "fix-failed-runs"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testEnable(OutputType format, MockServerClient mock) {
        mockDescribe(mock);
        mock.when(request().withMethod("POST").withPath("/agents/agt_1/enable").withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agent\":" + AGENT + "}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "enable", "-w", WSP_ID, "-i", "agt_1");

        assertOutput(format, out, new AgentUpdated(WSP_REF, "fix-failed-runs", "enabled"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDisable(OutputType format, MockServerClient mock) {
        mockFindByName(mock);
        mockDescribe(mock);
        mock.when(request().withMethod("POST").withPath("/agents/agt_1/disable").withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agent\":" + AGENT.replace("active", "inactive") + "}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "disable", "-w", WSP_ID, "-n", "fix-failed-runs");

        assertOutput(format, out, new AgentUpdated(WSP_REF, "fix-failed-runs", "disabled"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testLaunchConfiguredAgent(OutputType format, MockServerClient mock) {
        mockDescribe(mock);
        mock.when(request().withMethod("POST").withPath("/agents/launch").withQueryStringParameter("workspaceId", WSP_ID)
                        .withBody(json("{\"agentConfigId\":\"agt_1\",\"instructions\":\"Find the root cause\"}")), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agentRunId\":\"run_1\",\"status\":\"pending\"}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "launch", "-w", WSP_ID, "-i", "agt_1");

        assertOutput(format, out, new AgentLaunched(WSP_REF, "run_1", "pending"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testLaunchAdHocInstructions(OutputType format, MockServerClient mock) {
        mock.when(request().withMethod("POST").withPath("/agents/launch").withQueryStringParameter("workspaceId", WSP_ID)
                        .withBody(json("{\"instructions\":\"Summarize last week runs\"}", MatchType.STRICT)), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agentRunId\":\"run_2\",\"status\":\"pending\"}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "launch", "-w", WSP_ID, "--instructions", "Summarize last week runs");

        assertOutput(format, out, new AgentLaunched(WSP_REF, "run_2", "pending"));
    }

    @Test
    void testLaunchWithoutAgentOrInstructions(MockServerClient mock) {
        ExecOut out = exec(mock, "agents", "launch", "-w", WSP_ID);

        assertEquals(errorMessage(out.app, new TowerException("Specify an agent to launch (--id or --name) or the instructions to run (--instructions or --instructions-file)")), out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testRunsList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        String run = """
                {
                  "id": "run_1",
                  "title": "Debug run 4abc",
                  "status": "completed",
                  "trigger": {"type": "manual", "entityId": "agt_1"},
                  "workflowId": "4abc",
                  "workspaceId": 75887156211589,
                  "dateCreated": "2026-09-03T10:00:00Z",
                  "lastUpdated": "2026-09-03T10:05:00Z"
                }
                """;
        mock.when(request().withMethod("GET").withPath("/agents/runs")
                        .withQueryStringParameter("workspaceId", WSP_ID)
                        .withQueryStringParameter("search", "status:completed")
                        .withQueryStringParameter("max", "100")
                        .withQueryStringParameter("offset", "0"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"agentRuns\":[" + run + "],\"totalSize\":1}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "runs", "list", "-w", WSP_ID, "-f", "status:completed");

        assertOutput(format, out, new AgentRunsList(WSP_REF, List.of(parseJson(run, AgentRunDbDto.class)), PaginationInfo.from(new PaginationOptions(), 1L)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testRunsView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        String status = "{\"agentRunId\":\"run_1\",\"status\":\"running\",\"threadId\":\"thr_1\",\"sessionId\":\"ses_1\"}";
        mock.when(request().withMethod("GET").withPath("/agents/runs/run_1/status").withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(200).withBody(status).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "agents", "runs", "view", "-w", WSP_ID, "-i", "run_1");

        assertOutput(format, out, new AgentRunView(WSP_REF, parseJson(status, AgentRunStatusResponse.class)));
    }
}
