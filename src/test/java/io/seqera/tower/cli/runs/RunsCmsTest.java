package io.seqera.tower.cli.runs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.RunCanceled;
import io.seqera.tower.cli.responses.RunCreated;
import io.seqera.tower.cli.responses.RunDeleted;
import io.seqera.tower.cli.responses.RunList;
import io.seqera.tower.cli.responses.RunView;
import io.seqera.tower.model.ListWorkflowsResponseListWorkflowsElement;
import io.seqera.tower.model.Workflow;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static io.seqera.tower.cli.utils.JsonHelper.prettyJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class RunsCmsTest extends BaseCmdTest {

    @Test
    void testDelete(MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "runs", "delete", "-i", "5dAZoXrcmZXRO4");

        assertEquals("", out.stdErr);
        assertEquals(new RunDeleted("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testDeleteNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(403)
        );

        ExecOut out = exec(mock, "runs", "delete", "-i", "5dAZoXrcmZXRO4");

        assertEquals(errorMessage(out.app, new RunNotFoundException("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testCancel(MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/workflow/5dAZoXrcmZXRO4/cancel"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "runs", "cancel", "-i", "5dAZoXrcmZXRO4");

        assertEquals("", out.stdErr);
        assertEquals(new RunCanceled("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCancelNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/workflow/5dAZoXrcmZXRO4/cancel"), exactly(1)
        ).respond(
                response().withStatusCode(403)
        );

        ExecOut out = exec(mock, "runs", "cancel", "-i", "5dAZoXrcmZXRO4");

        assertEquals(errorMessage(out.app, new RunNotFoundException("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testList(MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list");

        assertEquals("", out.stdErr);
        assertEquals(chop(new RunList(USER_WORKSPACE_NAME, Arrays.asList(
                parseJson(" {\n" +
                        "      \"starred\": false,\n" +
                        "      \"workflow\": {\n" +
                        "        \"id\": \"5mDfiUtqyptDib\",\n" +
                        "        \"commandLine\": \"nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main\",\n" +
                        "        \"ownerId\": 9,\n" +
                        "        \"submit\": \"2021-09-22T05:45:44Z\",\n" +
                        "        \"dateCreated\": \"2021-09-22T05:45:44Z\",\n" +
                        "        \"lastUpdated\": \"2021-09-22T05:45:44Z\",\n" +
                        "        \"runName\": \"spontaneous_easley\"\n" +
                        "      }\n" +
                        "    }", ListWorkflowsResponseListWorkflowsElement.class),
                parseJson(" {\n" +
                        "      \"starred\": false,\n" +
                        "      \"workflow\": {\n" +
                        "        \"id\": \"6mDfiUtqyptDib\",\n" +
                        "        \"commandLine\": \"nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main\",\n" +
                        "        \"ownerId\": 9,\n" +
                        "        \"submit\": \"2021-09-22T05:45:44Z\",\n" +
                        "        \"dateCreated\": \"2021-09-22T05:45:44Z\",\n" +
                        "        \"lastUpdated\": \"2021-09-22T05:45:44Z\",\n" +
                        "        \"runName\": \"spontaneous_easley\"\n" +
                        "      }\n" +
                        "    }", ListWorkflowsResponseListWorkflowsElement.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListEmpty(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/workflow"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"workflows\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list");

        assertEquals("", out.stdErr);
        assertEquals(chop(new RunList(USER_WORKSPACE_NAME, List.of()).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testView(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "view", "-i", "5dAZoXrcmZXRO4");

        Workflow workflow = parseJson("{\n" +
                "    \"id\": \"5mDfiUtqyptDib\",\n" +
                "    \"submit\": \"2021-09-22T05:45:44Z\",\n" +
                "    \"start\": null,\n" +
                "    \"complete\": null,\n" +
                "    \"dateCreated\": \"2021-09-22T05:45:44Z\",\n" +
                "    \"lastUpdated\": \"2021-09-22T05:45:44Z\",\n" +
                "    \"runName\": \"spontaneous_easley\",\n" +
                "    \"sessionId\": \"ecaad2dd-83bb-4e0d-9418-d84f177e1e74\",\n" +
                "    \"profile\": null,\n" +
                "    \"workDir\": \"s3://nextflow-ci/julio\",\n" +
                "    \"commitId\": null,\n" +
                "    \"userName\": \"jfernandez74\",\n" +
                "    \"scriptId\": null,\n" +
                "    \"revision\": \"main\",\n" +
                "    \"commandLine\": \"nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main\",\n" +
                "    \"projectName\": \"grananda/nextflow-hello\",\n" +
                "    \"scriptName\": null,\n" +
                "    \"launchId\": \"5SCyEXKrCqFoGzOXGpesr5\",\n" +
                "    \"status\": \"SUBMITTED\",\n" +
                "    \"configFiles\": null,\n" +
                "    \"params\": null,\n" +
                "    \"configText\": null,\n" +
                "    \"manifest\": null,\n" +
                "    \"nextflow\": null,\n" +
                "    \"stats\": null,\n" +
                "    \"errorMessage\": null,\n" +
                "    \"errorReport\": null,\n" +
                "    \"deleted\": null,\n" +
                "    \"peakLoadCpus\": null,\n" +
                "    \"peakLoadTasks\": null,\n" +
                "    \"peakLoadMemory\": null,\n" +
                "    \"projectDir\": null,\n" +
                "    \"homeDir\": null,\n" +
                "    \"container\": null,\n" +
                "    \"repository\": null,\n" +
                "    \"containerEngine\": null,\n" +
                "    \"scriptFile\": null,\n" +
                "    \"launchDir\": null,\n" +
                "    \"duration\": null,\n" +
                "    \"exitStatus\": null,\n" +
                "    \"resume\": false,\n" +
                "    \"success\": null,\n" +
                "    \"logFile\": null,\n" +
                "    \"outFile\": null,\n" +
                "    \"operationId\": null,\n" +
                "    \"ownerId\": 9\n" +
                "  }", Workflow.class);

        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new RunView("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME, workflow).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testViewAsJson(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "--json", "runs", "view", "-i", "5dAZoXrcmZXRO4");

        Workflow workflow = parseJson("{\n" +
                "    \"id\": \"5mDfiUtqyptDib\",\n" +
                "    \"submit\": \"2021-09-22T05:45:44Z\",\n" +
                "    \"start\": null,\n" +
                "    \"complete\": null,\n" +
                "    \"dateCreated\": \"2021-09-22T05:45:44Z\",\n" +
                "    \"lastUpdated\": \"2021-09-22T05:45:44Z\",\n" +
                "    \"runName\": \"spontaneous_easley\",\n" +
                "    \"sessionId\": \"ecaad2dd-83bb-4e0d-9418-d84f177e1e74\",\n" +
                "    \"profile\": null,\n" +
                "    \"workDir\": \"s3://nextflow-ci/julio\",\n" +
                "    \"commitId\": null,\n" +
                "    \"userName\": \"jfernandez74\",\n" +
                "    \"scriptId\": null,\n" +
                "    \"revision\": \"main\",\n" +
                "    \"commandLine\": \"nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main\",\n" +
                "    \"projectName\": \"grananda/nextflow-hello\",\n" +
                "    \"scriptName\": null,\n" +
                "    \"launchId\": \"5SCyEXKrCqFoGzOXGpesr5\",\n" +
                "    \"status\": \"SUBMITTED\",\n" +
                "    \"configFiles\": null,\n" +
                "    \"params\": null,\n" +
                "    \"configText\": null,\n" +
                "    \"manifest\": null,\n" +
                "    \"nextflow\": null,\n" +
                "    \"stats\": null,\n" +
                "    \"errorMessage\": null,\n" +
                "    \"errorReport\": null,\n" +
                "    \"deleted\": null,\n" +
                "    \"peakLoadCpus\": null,\n" +
                "    \"peakLoadTasks\": null,\n" +
                "    \"peakLoadMemory\": null,\n" +
                "    \"projectDir\": null,\n" +
                "    \"homeDir\": null,\n" +
                "    \"container\": null,\n" +
                "    \"repository\": null,\n" +
                "    \"containerEngine\": null,\n" +
                "    \"scriptFile\": null,\n" +
                "    \"launchDir\": null,\n" +
                "    \"duration\": null,\n" +
                "    \"exitStatus\": null,\n" +
                "    \"resume\": false,\n" +
                "    \"success\": null,\n" +
                "    \"logFile\": null,\n" +
                "    \"outFile\": null,\n" +
                "    \"operationId\": null,\n" +
                "    \"ownerId\": 9\n" +
                "  }", Workflow.class);

        assertEquals("", out.stdErr);
        assertEquals(prettyJson(new RunView("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME, workflow).getJSON()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testViewNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(403)
        );

        ExecOut out = exec(mock, "runs", "view", "-i", "5dAZoXrcmZXRO4");

        assertEquals(errorMessage(out.app, new RunNotFoundException("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testRelaunch(MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("POST").withPath("/workflow/launch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"workflowId\": \"5UVJlhfUAHTuAP\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/pipelines/39121971175999/launch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_relaunch_pipeline_description")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "relaunch", "-i", "39121971175999");


        assertEquals("", out.stdErr);
        assertEquals(new RunCreated("5UVJlhfUAHTuAP", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testInvalidAuth(MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(401)
        );

        ExecOut out = exec(mock, "runs", "delete", "-i", "5dAZoXrcmZXRO4");

        assertEquals(errorMessage(out.app, new ApiException(401, "Unauthorized")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testShowUsage(MockServerClient mock) {

        ExecOut out = exec(mock, "runs");

        if (out.app != null) {
            assertEquals(errorMessage(out.app, new ShowUsageException()), out.stdErr);
            assertEquals("", out.stdOut);
            assertEquals(-1, out.exitCode);
        }
    }
}