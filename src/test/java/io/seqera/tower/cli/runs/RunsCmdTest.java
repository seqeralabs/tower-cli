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
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.runs.download.enums.RunDownloadFileType;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.runs.RunCanceled;
import io.seqera.tower.cli.responses.runs.RunDeleted;
import io.seqera.tower.cli.responses.runs.RunFileDownloaded;
import io.seqera.tower.cli.responses.runs.RunList;
import io.seqera.tower.cli.responses.runs.RunSubmited;
import io.seqera.tower.cli.responses.runs.RunView;
import io.seqera.tower.model.ListWorkflowsResponseListWorkflowsElement;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class RunsCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "runs", "delete", "-i", "5dAZoXrcmZXRO4");
        assertOutput(format, out, new RunDeleted("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME));
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
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testCancel(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/workflow/5dAZoXrcmZXRO4/cancel"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "runs", "cancel", "-i", "5dAZoXrcmZXRO4");
        assertOutput(format, out, new RunCanceled("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME));
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
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "runs", "list");
        assertOutput(format, out, new RunList(USER_WORKSPACE_NAME, Arrays.asList(
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
        ), baseUserUrl(mock, USER_WORKSPACE_NAME)));
    }

    @Test
    void testListWithOffset(MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow")
                        .withQueryStringParameter("offset", "1")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list", "--offset", "1", "--max", "2");

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
        ), baseUserUrl(mock, USER_WORKSPACE_NAME)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListWithPage(MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list", "--page", "1", "--max", "2");

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
        ), baseUserUrl(mock, USER_WORKSPACE_NAME)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListWithConflictingPageable(MockServerClient mock) {

        ExecOut out = exec(mock, "runs", "list", "--page", "1", "--offset", "0", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --page or --offset as pagination parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testListEmpty(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/workflow"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"workflows\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list");

        assertEquals("", out.stdErr);
        assertEquals(chop(new RunList(USER_WORKSPACE_NAME, List.of(), baseUserUrl(mock, USER_WORKSPACE_NAME)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4/progress"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_progress")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/launch/5SCyEXKrCqFoGzOXGpesr5"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("launch_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/isnEDBLvHDAIteOEF44ow"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "runs", "view", "-i", "5dAZoXrcmZXRO4");

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

        WorkflowLoad workflowLoad = parseJson("{\n" +
                "      \"cpus\": 0,\n" +
                "      \"cpuTime\": 0,\n" +
                "      \"cpuLoad\": 0,\n" +
                "      \"memoryRss\": 0,\n" +
                "      \"memoryReq\": 0,\n" +
                "      \"readBytes\": 0,\n" +
                "      \"writeBytes\": 0,\n" +
                "      \"volCtxSwitch\": 0,\n" +
                "      \"invCtxSwitch\": 0,\n" +
                "      \"cost\": null,\n" +
                "      \"loadTasks\": 0,\n" +
                "      \"loadCpus\": 0,\n" +
                "      \"loadMemory\": 0,\n" +
                "      \"peakCpus\": 0,\n" +
                "      \"peakTasks\": 0,\n" +
                "      \"peakMemory\": 0,\n" +
                "      \"executors\": null,\n" +
                "      \"dateCreated\": null,\n" +
                "      \"lastUpdated\": null,\n" +
                "      \"cached\": 0,\n" +
                "      \"pending\": 0,\n" +
                "      \"submitted\": 0,\n" +
                "      \"running\": 0,\n" +
                "      \"succeeded\": 0,\n" +
                "      \"failed\": 0,\n" +
                "      \"memoryEfficiency\": 0,\n" +
                "      \"cpuEfficiency\": 0\n" +
                "    }", WorkflowLoad.class);

        Map<String, Object> general = new LinkedHashMap<>();
        general.put("id", workflow.getId());
        general.put("runName", workflow.getRunName());
        general.put("startingDate", workflow.getStart());
        general.put("commitId", workflow.getCommitId());
        general.put("sessionId", workflow.getSessionId());
        general.put("username", workflow.getUserName());
        general.put("workdir", workflow.getWorkDir());
        general.put("container", workflow.getContainer());
        general.put("executors", workflowLoad.getExecutors() != null ? String.join(", ", workflowLoad.getExecutors()) : null);
        general.put("computeEnv", "ce-aws-144996268157965");
        general.put("nextflowVersion", workflow.getNextflow() != null ? workflow.getNextflow().getVersion() : null);
        general.put("status", workflow.getStatus());


        List<String> configFiles = new ArrayList<>();
        String configText = null;
        Map<String, Object> params = new HashMap<>();
        String command = null;
        Map<String, Object> status = new HashMap<>();
        List<Map<String, Object>> processes = new ArrayList<>();
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> load = new HashMap<>();
        Map<String, Object> utilization = new HashMap<>();

        assertOutput(format, out, new RunView(
                USER_WORKSPACE_NAME,
                general,
                configFiles,
                configText,
                params,
                command,
                status,
                processes,
                stats,
                load,
                utilization,
                baseUserUrl(mock, USER_WORKSPACE_NAME)
        ));
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
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testRelaunch(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/workflow/launch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_launch")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5UVJlhfUAHTuAP"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/launch/5SCyEXKrCqFoGzOXGpesr5"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("launch_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(format, mock, "runs", "relaunch", "-i", "5UVJlhfUAHTuAP");
        assertOutput(format, out, new RunSubmited("35aLiS0bIM5efd", String.format("%s/user/jordi/watch/35aLiS0bIM5efd", url(mock)), "user"));
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
        assertEquals(1, out.exitCode);
    }

    @Test
    void testShowUsage(MockServerClient mock) {

        ExecOut out = exec(mock, "runs");

        if (out.app != null) {
            assertEquals(errorMessage(out.app, new ShowUsageException(out.app.spec.subcommands().get("runs").getCommandSpec())), out.stdErr);
            assertEquals("", out.stdOut);
            assertEquals(1, out.exitCode);
        }
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDownloadLog(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4/download")
                        .withQueryStringParameter("fileName", "nf-5dAZoXrcmZXRO4.txt"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/download", "txt")).withContentType(MediaType.APPLICATION_BINARY)
        );

        File file = new File(tempFile(new String(loadResource("runs/download", "txt")), "5dAZoXrcmZXRO", "txt"));

        ExecOut out = exec(format, mock, "runs", "view", "-i", "5dAZoXrcmZXRO4", "download");
        assertOutput(format, out, new RunFileDownloaded(file, RunDownloadFileType.stdout));
    }

    @Test
    void testDownloadTaskLog(MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/workflow/5dAZoXrcmZXRO4/download/5")
                        .withQueryStringParameter("fileName", ".command.out"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/download-task", "txt")).withContentType(MediaType.APPLICATION_BINARY)
        );

        File file = new File(tempFile(new String(loadResource("runs/download-task", "txt")), "5dAZoXrcmZXRO", "txt"));

        ExecOut out = exec(mock, "runs", "view", "-i", "5dAZoXrcmZXRO4", "download", "-t", "5");
        assertEquals("", out.stdErr);
        assertEquals(new RunFileDownloaded(file, RunDownloadFileType.stdout).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}