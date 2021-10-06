package io.seqera.tower.cli.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.exceptions.ActionNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.actions.ActionCreate;
import io.seqera.tower.cli.responses.actions.ActionUpdate;
import io.seqera.tower.cli.responses.actions.ActionsDelete;
import io.seqera.tower.cli.responses.actions.ActionsLaunch;
import io.seqera.tower.cli.responses.actions.ActionsList;
import io.seqera.tower.cli.responses.actions.ActionsPause;
import io.seqera.tower.cli.responses.actions.ActionsView;
import io.seqera.tower.model.Action;
import io.seqera.tower.model.ListActionsResponseActionInfo;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class ActionsCmdTest extends BaseCmdTest {

    @Test
    void testActionsList(MockServerClient mock) throws JsonProcessingException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "list");

        assertEquals(chop(new ActionsList(Arrays.asList(parseJson(" {\n" +
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
        ), "jordi").toString()), out.stdOut);
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testActionsListEmpty(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{ \"actions\": [] }").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "list");

        assertEquals(chop(new ActionsList(List.of(), "jordi").toString()), out.stdOut);
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testActionView(MockServerClient mock) throws JsonProcessingException {
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

        ExecOut out = exec(mock, "actions", "view", "-n", "hello");

        assertEquals(chop(new ActionsView(parseJson("{\n" +
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
                "  }", Action.class)).toString()), out.stdOut);
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testActionViewNoActionFound(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "view", "-n", "test");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new ActionNotFoundException("test", USER_WORKSPACE_NAME)), out.stdErr);
    }

    @Test
    void testActionViewNoWorkspaceActionsFound(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{ \"actions\": [] }").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "view", "-n", "test");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new ActionNotFoundException("test", USER_WORKSPACE_NAME)), out.stdErr);
    }

    @Test
    void testActionDelete(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "actions", "delete", "-n", "hello");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new ActionsDelete("hello", USER_WORKSPACE_NAME).toString(), out.stdOut);
    }

    @Test
    void testActionDeleteError(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/actions/57byWxhmUDLLWIF4J97XEP"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "delete", "-n", "hello");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("Unable to delete action '%s' for workspace '%s'", "hello", USER_WORKSPACE_NAME))), out.stdErr);
    }

    @Test
    void testActionDeleteNotFound(MockServerClient mock) {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(mock, "actions", "delete", "-n", "test");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new ActionNotFoundException("test", USER_WORKSPACE_NAME)), out.stdErr);
    }

    @Test
    void testActionLaunch(MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions/57byWxhmUDLLWIF4J97XEP/launch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/action_launch")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "launch", "-n", "hello", "--params", tempFile("{ \"timeout\": 60 }", "params", ".json"));

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new ActionsLaunch("hello", USER_WORKSPACE_NAME, "QM0fP9K31kMLe").toString(), out.stdOut);
    }

    @Test
    void testActionLaunchError(MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions/57byWxhmUDLLWIF4J97XEP/launch"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "launch", "-n", "hello", "--params", tempFile("{ \"timeout\": 60 }", "params", ".json"));

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("An error has occur while launching action '%s'", "hello"))), out.stdErr);
    }

    @Test
    void testActionPause(MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions/57byWxhmUDLLWIF4J97XEP/pause"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "actions", "pause", "-n", "hello", "--params", tempFile("{ \"param\": 1 }", "params", ".json"));

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new ActionsPause("hello", USER_WORKSPACE_NAME).toString(), out.stdOut);
    }

    @Test
    void testActionPauseError(MockServerClient mock) throws IOException {
        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("actions/actions_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/actions/57byWxhmUDLLWIF4J97XEP/pause"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "pause", "-n", "hello", "--params", tempFile("{ \"timeout\": 60 }", "params", ".json"));

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("An error has occur while pausing action '%s'", "hello"))), out.stdErr);
    }

    @Test
    void testActionCreate(MockServerClient mock) {
        mock.reset();

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
                request().withMethod("POST").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("/actions/action_create")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "actions", "create", "-n", "new-action", "--source", "github", "--pipeline", "https://github.com/pditommaso/nf-sleep");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new ActionCreate("new-action", USER_WORKSPACE_NAME, "2Z1g6MCWpOLgHLA65cw1qt").toString(), out.stdOut);
    }

    @Test
    void testActionCreateWithError(MockServerClient mock) {
        mock.reset();

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
                request().withMethod("POST").withPath("/actions"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "actions", "create", "-n", "new-action", "--source", "github", "--pipeline", "https://github.com/pditommaso/nf-sleep");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("Unable to create action for workspace '%s'", USER_WORKSPACE_NAME))), out.stdErr);
    }

    @Test
    void testActionUpdate(MockServerClient mock) {
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

        ExecOut out = exec(mock, "actions", "update", "-n", "hello");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new ActionUpdate("hello", USER_WORKSPACE_NAME, "57byWxhmUDLLWIF4J97XEP").toString(), out.stdOut);
    }

    @Test
    void testActionUpdateWithError(MockServerClient mock) {
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
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException(String.format("Unable to update action '%s' for workspace '%s'", "hello", USER_WORKSPACE_NAME))), out.stdErr);
    }
}
