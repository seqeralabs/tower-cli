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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.ActionNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.actions.ActionAdd;
import io.seqera.tower.cli.responses.actions.ActionUpdate;
import io.seqera.tower.cli.responses.actions.ActionsDelete;
import io.seqera.tower.cli.responses.actions.ActionsList;
import io.seqera.tower.cli.responses.actions.ActionsView;
import io.seqera.tower.model.ActionResponseDto;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class ActionsCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "actions", "list");
        assertOutput(format, out, new ActionsList(Arrays.asList(parseJson(" {\n" +
                        "      \"id\": \"57byWxhmUDLLWIF4J97XEP\",\n" +
                        "      \"name\": \"hello\",\n" +
                        "      \"pipeline\": \"https://github.com/pditommaso/hello\",\n" +
                        "      \"source\": \"github\",\n" +
                        "      \"status\": \"ACTIVE\",\n" +
                        "      \"lastSeen\": \"2021-06-18T10:10:33Z\",\n" +
                        "      \"dateCreated\": \"2021-06-18T10:10:05Z\",\n" +
                        "      \"event\": {\n" +
                        "        \"@class\": \"io.seqera.tower.domain.github.GithubActionEvent\",\n" +
                        "        \"ref\": \"refs/heads/master\",\n" +
                        "        \"commitId\": \"af8d93083a36552914929a959f7576f62996032e\",\n" +
                        "        \"commitMessage\": \"Update README.md\",\n" +
                        "        \"pusherName\": \"pditommaso\",\n" +
                        "        \"pusherEmail\": \"paolo.ditommaso@gmail.com\",\n" +
                        "        \"timestamp\": \"2021-06-18T12:10:32+02:00\",\n" +
                        "        \"discriminator\": \"github\"\n" +
                        "      },\n" +
                        "      \"usageCmd\": null,\n" +
                        "      \"endpoint\": \"https://api.github.com/repos/pditommaso/hello/hooks/303166158\"\n" +
                        "    },", ListActionsResponseActionInfo.class),
                parseJson(" {\n" +
                        "      \"id\": \"58byWxhmUDLLWIF4J97XEP\",\n" +
                        "      \"name\": \"Bye\",\n" +
                        "      \"pipeline\": \"https://github.com/pditommaso/hello\",\n" +
                        "      \"source\": \"github\",\n" +
                        "      \"status\": \"ACTIVE\",\n" +
                        "      \"lastSeen\": \"2021-06-18T10:10:33Z\",\n" +
                        "      \"dateCreated\": \"2021-06-18T10:10:05Z\",\n" +
                        "      \"event\": {\n" +
                        "        \"@class\": \"io.seqera.tower.domain.github.GithubActionEvent\",\n" +
                        "        \"ref\": \"refs/heads/master\",\n" +
                        "        \"commitId\": \"af8d93083a36552914929a959f7576f62996032e\",\n" +
                        "        \"commitMessage\": \"Update README.md\",\n" +
                        "        \"pusherName\": \"pditommaso\",\n" +
                        "        \"pusherEmail\": \"paolo.ditommaso@gmail.com\",\n" +
                        "        \"timestamp\": \"2021-06-18T12:10:32+02:00\",\n" +
                        "        \"discriminator\": \"github\"\n" +
                        "      },\n" +
                        "      \"usageCmd\": null,\n" +
                        "      \"endpoint\": \"https://api.github.com/repos/pditommaso/hello/hooks/303166158\"\n" +
                        "    }", ListActionsResponseActionInfo.class)
        ), "jordi", baseUserUrl(mock, "jordi"), false));
    }

    @Test
    void testListEmpty(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{ \"actions\": [] }").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "list");

        assertEquals(chop(new ActionsList(List.of(), "jordi", baseUserUrl(mock, "jordi"), false).toString()), out.stdOut);
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "actions", "view", "-n", "hello");
        assertOutput(format, out, new ActionsView(parseJson("{\n" +
                "    \"id\": \"57byWxhmUDLLWIF4J97XEP\",\n" +
                "    \"launch\": {\n" +
                "      \"id\": \"3htPtgK2KufwvQcovOko\",\n" +
                "      \"computeEnv\": {\n" +
                "        \"id\": \"1NcvsrdHeaKsrpgQ85NYpe\",\n" +
                "        \"name\": \"deleted-774901692352490\",\n" +
                "        \"description\": null,\n" +
                "        \"platform\": \"slurm-platform\",\n" +
                "        \"config\": {\n" +
                "          \"hostName\": \"slurm.seqera.io\",\n" +
                "          \"workDir\": \"/home/ubuntu/nf-work\",\n" +
                "          \"userName\": \"ubuntu\",\n" +
                "          \"preRunScript\": null,\n" +
                "          \"postRunScript\": null,\n" +
                "          \"headQueue\": \"long\",\n" +
                "          \"launchDir\": \"/home/ubuntu/nf-work\",\n" +
                "          \"headJobOptions\": null,\n" +
                "          \"computeQueue\": null,\n" +
                "          \"port\": null,\n" +
                "          \"maxQueueSize\": null,\n" +
                "          \"discriminator\": \"slurm-platform\"\n" +
                "        },\n" +
                "        \"dateCreated\": \"2021-06-18T08:58:21Z\",\n" +
                "        \"lastUpdated\": \"2021-06-18T08:58:21Z\",\n" +
                "        \"lastUsed\": \"2021-06-18T10:22:31Z\",\n" +
                "        \"deleted\": true,\n" +
                "        \"status\": \"AVAILABLE\",\n" +
                "        \"message\": null,\n" +
                "        \"primary\": null,\n" +
                "        \"credentialsId\": \"5KmzwVT34sT8ItofmP5S\"\n" +
                "      },\n" +
                "      \"pipeline\": \"https://github.com/pditommaso/hello\",\n" +
                "      \"workDir\": \"/home/ubuntu/nf-work\",\n" +
                "      \"revision\": null,\n" +
                "      \"configText\": null,\n" +
                "      \"paramsText\": null,\n" +
                "      \"preRunScript\": null,\n" +
                "      \"postRunScript\": null,\n" +
                "      \"mainScript\": null,\n" +
                "      \"entryName\": null,\n" +
                "      \"schemaName\": null,\n" +
                "      \"resume\": false,\n" +
                "      \"pullLatest\": false,\n" +
                "      \"stubRun\": false,\n" +
                "      \"sessionId\": null,\n" +
                "      \"configProfiles\": null,\n" +
                "      \"dateCreated\": \"2021-06-18T10:10:05Z\",\n" +
                "      \"lastUpdated\": \"2021-06-18T10:10:05Z\"\n" +
                "    },\n" +
                "    \"name\": \"hello\",\n" +
                "    \"hookId\": \"303166158\",\n" +
                "    \"hookUrl\": \"https://api.github.com/repos/pditommaso/hello/hooks/303166158\",\n" +
                "    \"message\": null,\n" +
                "    \"deleted\": null,\n" +
                "    \"source\": \"github\",\n" +
                "    \"status\": \"ACTIVE\",\n" +
                "    \"config\": {\n" +
                "      \"events\": [\n" +
                "        \"push\"\n" +
                "      ],\n" +
                "      \"discriminator\": \"github\"\n" +
                "    },\n" +
                "    \"event\": {\n" +
                "      \"ref\": \"refs/heads/master\",\n" +
                "      \"commitId\": \"af8d93083a36552914929a959f7576f62996032e\",\n" +
                "      \"commitMessage\": \"Update README.md\",\n" +
                "      \"pusherName\": \"pditommaso\",\n" +
                "      \"pusherEmail\": \"paolo.ditommaso@gmail.com\",\n" +
                "      \"timestamp\": \"2021-06-18T12:10:32+02:00\",\n" +
                "      \"discriminator\": \"github\"\n" +
                "    },\n" +
                "    \"lastSeen\": \"2021-06-18T10:10:33Z\",\n" +
                "    \"dateCreated\": \"2021-06-18T10:10:05Z\",\n" +
                "    \"lastUpdated\": \"2021-06-18T10:10:33Z\"\n" +
                "  }", ActionResponseDto.class), baseUserUrl(mock, USER_WORKSPACE_NAME)));
    }

    @Test
    void testViewNoActionFound(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "view", "-n", "test");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new ActionNotFoundException("test", USER_WORKSPACE_NAME)), out.stdErr);
    }

    @Test
    void testViewNoWorkspaceActionsFound(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{ \"actions\": [] }").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "view", "-n", "test");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new ActionNotFoundException("test", USER_WORKSPACE_NAME)), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "actions", "delete", "-n", "hello");
        assertOutput(format, out, new ActionsDelete("hello", USER_WORKSPACE_NAME));
    }

    @Test
    void testDeleteError(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "delete", "-n", "hello");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("Unable to delete action '%s' for workspace '%s'", "hello", USER_WORKSPACE_NAME))), out.stdErr);
    }

    @Test
    void testDeleteNotFound(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(mock, "actions", "delete", "-n", "test");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new ActionNotFoundException("test", USER_WORKSPACE_NAME)), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":true,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("/actions/action_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "actions", "add", "github", "-n", "new-action", "--pipeline", "https://github.com/pditommaso/nf-sleep");
        assertOutput(format, out, new ActionAdd("new-action", USER_WORKSPACE_NAME, "2Z1g6MCWpOLgHLA65cw1qt"));
    }

    @Test
    void testAddWithTowerConfig(MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"primary\":true}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions")
                        .withBody(json("{\"launch\":{\"towerConfig\":\"reports: {}\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("/actions/action_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "add", "github", "-n", "new-action", "--pipeline", "https://github.com/pditommaso/nf-sleep",
                "--tower-config", tempFile("reports: {}", "tower", "yml"));

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddWithOverwrite(OutputType format, MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":true,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("/actions/action_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "actions", "add", "github", "--overwrite", "-n", "hello", "--pipeline", "https://github.com/pditommaso/nf-sleep");
        assertOutput(format, out, new ActionAdd("hello", USER_WORKSPACE_NAME, "2Z1g6MCWpOLgHLA65cw1qt"));
    }

    @Test
    void testAddWithError(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":true,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "add", "github", "-n", "new-action", "--pipeline", "https://github.com/pditommaso/nf-sleep");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("Unable to add action for workspace '%s'", USER_WORKSPACE_NAME))), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdate(OutputType format, MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":null,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "actions", "update", "-n", "hello");
        assertOutput(format, out, new ActionUpdate("hello", USER_WORKSPACE_NAME, "57byWxhmUDLLWIF4J97XEP"));
    }

    @Test
    void testUpdateWithError(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":null,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "update", "-n", "hello");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("Unable to update action '%s' for workspace '%s'", "hello", USER_WORKSPACE_NAME))), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateName(OutputType format, MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":null,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP")
                        .withBody(json("{ \"name\": \"hello_world\" }", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/validate")
                        .withQueryStringParameter("name", "hello_world"),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "actions", "update", "-n", "hello", "--new-name", "hello_world");
        assertOutput(format, out, new ActionUpdate("hello", USER_WORKSPACE_NAME, "57byWxhmUDLLWIF4J97XEP"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateInvalidName(OutputType format, MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":null,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP"),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/validate")
                        .withQueryStringParameter("name", "#hello"),
                exactly(1)
        ).respond(
                response().withStatusCode(400)
        );

        ExecOut out = exec(format, mock, "actions", "update", "-n", "hello", "--new-name", "#hello");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("Action name '%s' is not valid", "#hello"))), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testPause(OutputType format, MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":null,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions/57byWxhmUDLLWIF4J97XEP/pause"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "actions", "update", "-n", "hello", "-s", "pause");
        assertOutput(format, out, new ActionUpdate("hello", USER_WORKSPACE_NAME, "57byWxhmUDLLWIF4J97XEP"));
    }

    @Test
    void testPauseAlreadyPausedItem(MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":null,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "actions", "update", "-n", "hello", "-s", "active");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("The action is already set to '%s'", "ACTIVE"))), out.stdErr);
    }


    @Test
    void testPauseError(MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":null,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions/57byWxhmUDLLWIF4J97XEP/pause"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "update", "-n", "hello", "-s", "pause");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("An error has occur while setting the action '%s' to '%s'", "hello", "PAUSE"))), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddCron(OutputType format, MockServerClient mock) {
        mock.reset();
        mockPrimaryComputeEnv(mock);

        mock.when(
                request().withMethod("POST").withPath("/actions")
                        .withBody(json("{\"name\": \"nightly\", \"source\": \"cron\", \"cron\": {\"expression\": \"0 2 * * *\", \"timezone\": \"Europe/London\"}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("/actions/action_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "actions", "add", "cron", "-n", "nightly", "--pipeline", "https://github.com/pditommaso/nf-sleep", "--cron-expression", "0 2 * * *", "--timezone", "Europe/London");
        assertOutput(format, out, new ActionAdd("nightly", USER_WORKSPACE_NAME, "2Z1g6MCWpOLgHLA65cw1qt"));
    }

    @Test
    void testUpdateCron(MockServerClient mock) {
        mock.reset();
        mockActionLookup(mock, "cron", CRON_CONFIG);

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP")
                        .withBody(json("{\"name\": \"hello\", \"cron\": {\"expression\": \"*/15 * * * *\"}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "actions", "update", "-n", "hello", "--cron-expression", "*/15 * * * *");
        assertOutput(OutputType.console, out, new ActionUpdate("hello", USER_WORKSPACE_NAME, "57byWxhmUDLLWIF4J97XEP"));
    }

    @Test
    void testUpdateCronOptionsOnGithubAction(MockServerClient mock) {
        mock.reset();
        mockActionLookup(mock, "github", "{\"events\": [\"push\"], \"discriminator\": \"github\"}");

        ExecOut out = exec(mock, "actions", "update", "-n", "hello", "--timezone", "UTC");

        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException("Options --cron-expression and --timezone apply only to cron actions, but action 'hello' is a github action")), out.stdErr);
        mock.verify(request().withMethod("PUT"), VerificationTimes.never());
    }

    @Test
    void testViewCron(MockServerClient mock) {
        mock.reset();
        mockActionLookup(mock, "cron", CRON_CONFIG);
        mockUserInfo(mock);

        ExecOut out = exec(mock, "actions", "view", "-n", "hello");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertTrue(out.stdOut.contains("Cron expression | 0 2 * * *"), out.stdOut);
        assertTrue(out.stdOut.matches("(?s).*Timezone +\\| Europe/London.*"), out.stdOut);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddBucket(OutputType format, MockServerClient mock) {
        mock.reset();
        mockPrimaryComputeEnv(mock);

        mock.when(
                request().withMethod("POST").withPath("/actions")
                        .withBody(json("{\"name\": \"on-upload\", \"source\": \"bucket\", \"bucket\": {\"dataLinkId\": \"v1-user-abc\", \"markerFile\": \"incoming/.done\", \"events\": [\"object:created\", \"object:deleted\"]}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("/actions/action_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "actions", "add", "bucket", "-n", "on-upload", "--pipeline", "https://github.com/pditommaso/nf-sleep",
                "--data-link-id", "v1-user-abc", "--marker-file", "incoming/.done", "--events", "object:created,object:deleted");
        assertOutput(format, out, new ActionAdd("on-upload", USER_WORKSPACE_NAME, "2Z1g6MCWpOLgHLA65cw1qt"));
    }

    @Test
    void testUpdateBucket(MockServerClient mock) {
        mock.reset();
        mockActionLookup(mock, "bucket", BUCKET_CONFIG);

        mock.when(
                request().withMethod("PUT").withPath("/actions/57byWxhmUDLLWIF4J97XEP")
                        .withBody(json("{\"name\": \"hello\", \"bucket\": {\"markerFile\": \"signals/*.complete\"}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "actions", "update", "-n", "hello", "--marker-file", "signals/*.complete");
        assertOutput(OutputType.console, out, new ActionUpdate("hello", USER_WORKSPACE_NAME, "57byWxhmUDLLWIF4J97XEP"));
    }

    @Test
    void testUpdateBucketOptionsOnGithubAction(MockServerClient mock) {
        mock.reset();
        mockActionLookup(mock, "github", "{\"events\": [\"push\"], \"discriminator\": \"github\"}");

        ExecOut out = exec(mock, "actions", "update", "-n", "hello", "--events", "object:created");

        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException("Options --marker-file and --events apply only to bucket actions, but action 'hello' is a github action")), out.stdErr);
        mock.verify(request().withMethod("PUT"), VerificationTimes.never());
    }

    @Test
    void testUpdateBucketOptionsOnCronAction(MockServerClient mock) {
        mock.reset();
        mockActionLookup(mock, "cron", CRON_CONFIG);

        ExecOut out = exec(mock, "actions", "update", "-n", "hello", "--events", "object:created");

        assertEquals(1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException("Options --marker-file and --events apply only to bucket actions, but action 'hello' is a cron action")), out.stdErr);
        mock.verify(request().withMethod("PUT"), VerificationTimes.never());
    }

    @Test
    void testViewBucket(MockServerClient mock) {
        mock.reset();
        mockActionLookup(mock, "bucket", BUCKET_CONFIG);
        mockUserInfo(mock);

        ExecOut out = exec(mock, "actions", "view", "-n", "hello");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertTrue(out.stdOut.matches("(?s).*Data link ID +\\| v1-user-abc.*"), out.stdOut);
        assertTrue(out.stdOut.matches("(?s).*Bucket +\\| my-bucket.*"), out.stdOut);
        assertTrue(out.stdOut.matches("(?s).*Marker file +\\| incoming/\\.done.*"), out.stdOut);
        assertTrue(out.stdOut.matches("(?s).*Events +\\| object:created, object:deleted.*"), out.stdOut);
    }

    private static final String BUCKET_CONFIG = "{\"dataLinkId\": \"v1-user-abc\", \"bucketName\": \"my-bucket\", \"markerFile\": \"incoming/.done\", \"events\": [\"object:created\", \"object:deleted\"], \"discriminator\": \"bucket\"}";

    @Test
    void testListNewSources(MockServerClient mock) {
        mock.reset();
        mockUserInfo(mock);

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"actions\": [" +
                        "{\"id\": \"1a\", \"name\": \"nightly\", \"source\": \"cron\", \"status\": \"ACTIVE\", \"endpoint\": null, \"config\": " + CRON_CONFIG + "}," +
                        "{\"id\": \"2b\", \"name\": \"on-upload\", \"source\": \"bucket\", \"status\": \"ERROR\", \"endpoint\": null, \"config\": " + BUCKET_CONFIG + "}" +
                        "]}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "list");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertTrue(out.stdOut.matches("(?s).*nightly .*cron.*"), out.stdOut);
        assertTrue(out.stdOut.matches("(?s).*on-upload .*bucket.*"), out.stdOut);
    }

    private static final String CRON_CONFIG = "{\"expression\": \"0 2 * * *\", \"timezone\": \"Europe/London\", \"discriminator\": \"cron\"}";

    private void mockPrimaryComputeEnv(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/compute-envs").withQueryStringParameter("status", "AVAILABLE"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvs\":[{\"id\":\"vYOK4vn7spw7bHHWBDXZ2\",\"name\":\"demo\",\"platform\":\"aws-batch\",\"status\":\"AVAILABLE\",\"message\":null,\"lastUsed\":null,\"primary\":true,\"workspaceName\":null,\"visibility\":null}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/vYOK4vn7spw7bHHWBDXZ2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_demo")).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    private void mockUserInfo(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    /** Serves action 'hello' as an action of the given source and trigger configuration. */
    private void mockActionLookup(MockServerClient mock, String source, String configJson) {
        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        String view = new String(loadResource("actions/action_view"))
                .replace("\"source\": \"github\"", "\"source\": \"" + source + "\"")
                .replaceFirst("(?m)^    \"config\": \\{[^}]*\\}", Matcher.quoteReplacement("\"config\": " + configJson));
        mock.when(
                request().withMethod("GET").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(view).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    @Test
    void testAddWithoutSubCommands(MockServerClient mock) {
        ExecOut out = exec(mock, "actions", "add");
        assertEquals(1, out.exitCode);
        assertTrue(out.stdErr.contains("Missing Required Subcommand"));
    }

//    @ParameterizedTest
//    @EnumSource(OutputType.class)
//    void testLabels(OutputType format, MockServerClient mock) throws IOException {
//        mock.reset();
//        mock.when(
//                request().withMethod("GET").withPath("/labels"),exactly(1)
//        ).respond(
//                response().withStatusCode(200)
//        );
//
//        ExecOut out = exec(mock,"actions", "labels","label1,label2,label3","-n","action");
//        assertOutput(format,out,new ManageLabels(LabelsSubcmdOptions.Operation.set.prettyName,"action","1",0l));
//    }
}
