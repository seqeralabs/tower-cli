/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.seqera.tower.cli;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class RunCmdTest {

    private static Integer PORT = 1080;
    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startServer() {
        mockServer = startClientAndServer(PORT);
    }

    @AfterAll
    public static void stopServer() {
        mockServer.stop();
    }

    public static CommandLine buildCmd(String token, Long workspaceId, StringWriter stdOut) {
        App app = new App(new AppConfig("http://localhost:1080", token, workspaceId));
        CommandLine cmd = App.buildCmd(app);
        cmd.setOut(new PrintWriter(stdOut));
        return cmd;
    }

    private byte[] loadResponse(String name) {
        try {
            return this.getClass().getResourceAsStream("/runcmd/" + name + ".json").readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    void testInvalidAuth() {

        // Create command line
        StringWriter stdOut = new StringWriter();
        CommandLine cmd = buildCmd("fake_auth_token", null, stdOut);

        // Create server expectation
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pipelines")
                        ,
                        exactly(1)
                )
                .respond(
                        response().withStatusCode(401)
                );

        // Run the command
        int exitCode = cmd.execute("run", "hello");

        // Assert results
        assertEquals(-1, exitCode);
        assertEquals("Unauthorized\n", stdOut.toString());
    }

    @Test
    void testPipelineNotfound() {
        // Create command line
        StringWriter stdOut = new StringWriter();
        CommandLine cmd = buildCmd("fake_auth_token", null, stdOut);

        // Create server expectation
        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pipelines")
                        ,
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(loadResponse("pipelines_none"))
                );

        // Run the command
        int exitCode = cmd.execute("run", "hello");

        // Assert results
        assertEquals(-1, exitCode);
        assertEquals("Pipeline 'hello' not found on this workspace.\n", stdOut.toString());

    }

    @Test
    void testMultiplePipelinesFound() {
        // Create command line
        StringWriter stdOut = new StringWriter();
        CommandLine cmd = buildCmd("fake_auth_token", null, stdOut);

        // Create server expectation
        new MockServerClient("127.0.0.1", PORT)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pipelines")
                        ,
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(loadResponse("pipelines_multiple"))
                );

        // Run the command
        int exitCode = cmd.execute("run", "hello");

        // Assert results
        assertEquals(-1, exitCode);
        assertEquals("Multiple pipelines match 'hello'\n", stdOut.toString());
    }

    @Test
    void testSubmitWorkspacePipeline() {
        // Create command line
        StringWriter stdOut = new StringWriter();
        CommandLine cmd = buildCmd("fake_auth_token", null, stdOut);

        // Create server expectation
        new MockServerClient("127.0.0.1", PORT)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pipelines")
                        ,
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody( loadResponse("pipelines_sarek"))
                );

        new MockServerClient("127.0.0.1", PORT)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/pipelines/250911634275687/launch")
                        ,
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody( loadResponse("pipeline_launch_describe"))
                );

        new MockServerClient("127.0.0.1", PORT)
                .when(
                        request()
                            .withMethod("POST")
                            .withPath("/workflow/launch")
                        ,
                        exactly(1)
                )
                .respond(
                        response()
                            .withStatusCode(200)
                            .withBody( loadResponse("workflow_launch"))
                );

        // Run the command
        int exitCode = cmd.execute("run", "sarek");

        // Assert results
        assertEquals(0, exitCode);
        assertEquals("class SubmitWorkflowLaunchResponse {\n" +
                "    workflowId: 35aLiS0bIM5efd\n" +
                "}\n", stdOut.toString());

    }

}
