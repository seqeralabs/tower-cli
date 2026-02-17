/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.runs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.seqera.tower.ApiException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.runs.download.enums.RunDownloadFileType;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.runs.RunCanceled;
import io.seqera.tower.cli.responses.runs.RunDeleted;
import io.seqera.tower.cli.responses.runs.RunDump;
import io.seqera.tower.cli.responses.runs.RunFileDownloaded;
import io.seqera.tower.cli.responses.runs.RunList;
import io.seqera.tower.cli.responses.runs.RunSubmited;
import io.seqera.tower.cli.responses.runs.RunView;
import io.seqera.tower.cli.shared.WorkflowMetadata;
import io.seqera.tower.cli.utils.JsonHelper;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.cli.utils.TarFileHelper;
import io.seqera.tower.model.DescribeLaunchResponse;
import io.seqera.tower.model.DescribeTaskResponse;
import io.seqera.tower.model.DescribeWorkflowLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.GetProgressResponse;
import io.seqera.tower.model.GetWorkflowMetricsResponse;
import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.ListTasksResponse;
import io.seqera.tower.model.ListWorkflowsResponseListWorkflowsElement;
import io.seqera.tower.model.ServiceInfo;
import io.seqera.tower.model.ServiceInfoResponse;
import io.seqera.tower.model.Task;
import io.seqera.tower.model.WorkflowLoad;
import io.seqera.tower.model.WorkflowMaxDbDto;
import io.seqera.tower.model.WorkflowMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "runs", "list");
        assertOutput(format, out, new RunList(USER_WORKSPACE_NAME, Arrays.asList(
                parseJson("""
                         {
                              "starred": false,
                              "workflow": {
                                "id": "5mDfiUtqyptDib",
                                "commandLine": "nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main",
                                "ownerId": 9,
                                "submit": "2021-09-22T05:45:44Z",
                                "dateCreated": "2021-09-22T05:45:44Z",
                                "lastUpdated": "2021-09-22T05:45:44Z",
                                "runName": "spontaneous_easley"
                              }
                            }\
                        """, ListWorkflowsResponseListWorkflowsElement.class),
                parseJson("""
                         {
                              "starred": false,
                              "workflow": {
                                "id": "6mDfiUtqyptDib",
                                "commandLine": "nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main",
                                "ownerId": 9,
                                "submit": "2021-09-22T05:45:44Z",
                                "dateCreated": "2021-09-22T05:45:44Z",
                                "lastUpdated": "2021-09-22T05:45:44Z",
                                "runName": "spontaneous_easley"
                              }
                            }\
                        """, ListWorkflowsResponseListWorkflowsElement.class)
        ), baseUserUrl(mock, USER_WORKSPACE_NAME), false, null));
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
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list", "--offset", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new RunList(USER_WORKSPACE_NAME, Arrays.asList(
                parseJson("""
                         {
                              "starred": false,
                              "workflow": {
                                "id": "5mDfiUtqyptDib",
                                "commandLine": "nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main",
                                "ownerId": 9,
                                "submit": "2021-09-22T05:45:44Z",
                                "dateCreated": "2021-09-22T05:45:44Z",
                                "lastUpdated": "2021-09-22T05:45:44Z",
                                "runName": "spontaneous_easley"
                              }
                            }\
                        """, ListWorkflowsResponseListWorkflowsElement.class),
                parseJson("""
                         {
                              "starred": false,
                              "workflow": {
                                "id": "6mDfiUtqyptDib",
                                "commandLine": "nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main",
                                "ownerId": 9,
                                "submit": "2021-09-22T05:45:44Z",
                                "dateCreated": "2021-09-22T05:45:44Z",
                                "lastUpdated": "2021-09-22T05:45:44Z",
                                "runName": "spontaneous_easley"
                              }
                            }\
                        """, ListWorkflowsResponseListWorkflowsElement.class)
        ), baseUserUrl(mock, USER_WORKSPACE_NAME), false, PaginationInfo.from(1, 2)).toString()), out.stdOut);
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
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list", "--page", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new RunList(USER_WORKSPACE_NAME, Arrays.asList(
                parseJson("""
                         {
                              "starred": false,
                              "workflow": {
                                "id": "5mDfiUtqyptDib",
                                "commandLine": "nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main",
                                "ownerId": 9,
                                "submit": "2021-09-22T05:45:44Z",
                                "dateCreated": "2021-09-22T05:45:44Z",
                                "lastUpdated": "2021-09-22T05:45:44Z",
                                "runName": "spontaneous_easley"
                              }
                            }\
                        """, ListWorkflowsResponseListWorkflowsElement.class),
                parseJson("""
                         {
                              "starred": false,
                              "workflow": {
                                "id": "6mDfiUtqyptDib",
                                "commandLine": "nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main",
                                "ownerId": 9,
                                "submit": "2021-09-22T05:45:44Z",
                                "dateCreated": "2021-09-22T05:45:44Z",
                                "lastUpdated": "2021-09-22T05:45:44Z",
                                "runName": "spontaneous_easley"
                              }
                            }\
                        """, ListWorkflowsResponseListWorkflowsElement.class)
        ), baseUserUrl(mock, USER_WORKSPACE_NAME), false, PaginationInfo.from(null, 2, 1, null)).toString()), out.stdOut);
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
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list");

        assertEquals("", out.stdErr);
        assertEquals(chop(new RunList(USER_WORKSPACE_NAME, List.of(), baseUserUrl(mock, USER_WORKSPACE_NAME), false, null).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/progress"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_progress")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/launch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("launch_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/3xkkzYH2nbD3nZjrzKm0oR"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "runs", "view", "-i", "5mDfiUtqyptDib");

        WorkflowMaxDbDto workflow = parseJson("""
                {
                    "id": "5mDfiUtqyptDib",
                    "submit": "2021-09-22T05:45:44Z",
                    "start": null,
                    "complete": null,
                    "dateCreated": "2021-09-22T05:45:44Z",
                    "lastUpdated": "2021-09-22T05:45:44Z",
                    "runName": "spontaneous_easley",
                    "sessionId": "ecaad2dd-83bb-4e0d-9418-d84f177e1e74",
                    "profile": null,
                    "workDir": "s3://nextflow-ci/julio",
                    "commitId": null,
                    "userName": "jfernandez74",
                    "scriptId": null,
                    "revision": "main",
                    "commandLine": "nextflow run https://github.com/grananda/nextflow-hello -name spontaneous_easley -with-tower https://scratch.staging-tower.xyz/api -r main",
                    "projectName": "grananda/nextflow-hello",
                    "scriptName": null,
                    "launchId": "5SCyEXKrCqFoGzOXGpesr5",
                    "status": "SUBMITTED",
                    "configFiles": null,
                    "params": null,
                    "configText": null,
                    "manifest": null,
                    "nextflow": null,
                    "stats": null,
                    "errorMessage": null,
                    "errorReport": null,
                    "deleted": null,
                    "peakLoadCpus": null,
                    "peakLoadTasks": null,
                    "peakLoadMemory": null,
                    "projectDir": null,
                    "homeDir": null,
                    "container": null,
                    "repository": null,
                    "containerEngine": null,
                    "scriptFile": null,
                    "launchDir": null,
                    "duration": null,
                    "exitStatus": null,
                    "resume": false,
                    "success": null,
                    "logFile": null,
                    "outFile": null,
                    "operationId": null,
                    "ownerId": 9
                  }""", WorkflowMaxDbDto.class);

        WorkflowLoad workflowLoad = parseJson("""
                {
                      "cpus": 0,
                      "cpuTime": 0,
                      "cpuLoad": 0,
                      "memoryRss": 0,
                      "memoryReq": 0,
                      "readBytes": 0,
                      "writeBytes": 0,
                      "volCtxSwitch": 0,
                      "invCtxSwitch": 0,
                      "cost": null,
                      "loadTasks": 0,
                      "loadCpus": 0,
                      "loadMemory": 0,
                      "peakCpus": 0,
                      "peakTasks": 0,
                      "peakMemory": 0,
                      "executors": null,
                      "dateCreated": null,
                      "lastUpdated": null,
                      "cached": 0,
                      "pending": 0,
                      "submitted": 0,
                      "running": 0,
                      "succeeded": 0,
                      "failed": 0,
                      "memoryEfficiency": 0,
                      "cpuEfficiency": 0
                    }""", WorkflowLoad.class);

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
        general.put("labels", "");


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
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/launch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("runs/workflow_launch")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );


        ExecOut out = exec(format, mock, "runs", "relaunch", "-i", "5mDfiUtqyptDib");
        assertOutput(format, out, new RunSubmited("35aLiS0bIM5efd", null, String.format("%s/user/jordi/watch/35aLiS0bIM5efd", url(mock)), "user"));
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

    @Test
    void testDumpRuns(MockServerClient mock) throws IOException {

        byte[] sampleUserInfoBytes = loadResource("user");
        byte[] sampleServiceInfoBytes = loadResource("info/service-info");
        byte[] sampleDescribeWorkflowBytes = loadResource("workflow_view");
        byte[] sampleWorkflowProgressBytes = loadResource("workflow_progress");
        byte[] sampleWorkflowLog = loadResource("runs/download", "txt");
        byte[] sampleWorkflowMetricsBytes = loadResource("runs/runs_metrics");
        byte[] sampleTaskListBytes = loadResource("runs/tasks_list_response");
        byte[] sampleWorkflowLaunchBytes = loadResource("runs/workflow_launch");
        byte[] sampleLaunchBytes = loadResource("launch_view");

        //DescribeUserResponse sampleUserInfo = fromJSON(sampleUserInfoBytes, DescribeUserResponse.class);
        ServiceInfoResponse sampleServiceInfo = fromJSON(sampleServiceInfoBytes, ServiceInfoResponse.class);
        DescribeWorkflowResponse sampleDescribeWorkflow = fromJSON(sampleDescribeWorkflowBytes, DescribeWorkflowResponse.class);
        GetProgressResponse sampleWorkflowProgress = fromJSON(sampleWorkflowProgressBytes, GetProgressResponse.class);
        GetWorkflowMetricsResponse sampleWorkflowMetrics = fromJSON(sampleWorkflowMetricsBytes, GetWorkflowMetricsResponse.class);
        ListTasksResponse sampleTaskList = fromJSON(sampleTaskListBytes, ListTasksResponse.class);
        DescribeWorkflowLaunchResponse sampleWorkflowLaunch = fromJSON(sampleWorkflowLaunchBytes, DescribeWorkflowLaunchResponse.class);
        DescribeLaunchResponse sampleLaunch = fromJSON(sampleLaunchBytes, DescribeLaunchResponse.class);


        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleUserInfoBytes).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/service-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleServiceInfoBytes).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET")
                        .withPath("/workflow/5mDfiUtqyptDib")
                        .withQueryStringParameter("attributes", "labels,optimized"),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleDescribeWorkflowBytes).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/progress"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleWorkflowProgressBytes).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/download")
                        .withQueryStringParameter("fileName", "nf-5mDfiUtqyptDib.log"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleWorkflowLog).withContentType(MediaType.APPLICATION_BINARY)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/metrics"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleWorkflowMetricsBytes).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/tasks"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleTaskListBytes).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workflow/5mDfiUtqyptDib/launch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleWorkflowLaunchBytes).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/launch/5SCyEXKrCqFoGzOXGpesr5"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(sampleLaunchBytes).withContentType(MediaType.APPLICATION_JSON)
        );


        File file = new File(tempFile("", "test-dump-runs", ".tar.gz"));
        String workflowRunId = "5mDfiUtqyptDib";

        ExecOut out = exec(mock, "runs", "dump", "-i", workflowRunId, "-o", file.getAbsolutePath(), "--silent");
        assertEquals("", out.stdErr);
        assertEquals(new RunDump(workflowRunId, "user", file.toPath()).toString(), out.stdOut);
        assertEquals(0, out.exitCode);

        var serviceInfoJsonContent = TarFileHelper.readContentFile(file.toPath(), "service-info.json");
        var workflowJsonContent = TarFileHelper.readContentFile(file.toPath(), "workflow.json");
        var wfMetadataJsonContent = TarFileHelper.readContentFile(file.toPath(), "workflow-metadata.json");
        var wfLoadJsonContent = TarFileHelper.readContentFile(file.toPath(), "workflow-load.json");
        var wfLaunchJsonContent = TarFileHelper.readContentFile(file.toPath(), "workflow-launch.json");
        var wfMetricsJsonContent = TarFileHelper.readContentFile(file.toPath(), "workflow-metrics.json");
        var wfTasksJsonContent = TarFileHelper.readContentFile(file.toPath(), "workflow-tasks.json");
        var nxfLogJsonContent = TarFileHelper.readContentFile(file.toPath(), "nextflow.log");

        assertTrue(serviceInfoJsonContent.isPresent());
        assertTrue(workflowJsonContent.isPresent());
        assertTrue(wfMetadataJsonContent.isPresent());
        assertTrue(wfLoadJsonContent.isPresent());
        assertTrue(wfLaunchJsonContent.isPresent());
        assertTrue(wfMetricsJsonContent.isPresent());
        assertTrue(wfTasksJsonContent.isPresent());
        assertTrue(nxfLogJsonContent.isPresent());

        // service-info.json
        {
            ServiceInfo actual = fromJSON(serviceInfoJsonContent.get(), ServiceInfo.class);
            assertEquals(actual, sampleServiceInfo.getServiceInfo());
        }

        // workflow.json
        {
            WorkflowMaxDbDto actual = fromJSON(workflowJsonContent.get(), WorkflowMaxDbDto.class);
            assertEquals(sampleDescribeWorkflow.getWorkflow(), actual);
        }

        // workflow-metadata.json
        {
            WorkflowMetadata actual = fromJSON(wfMetadataJsonContent.get(), WorkflowMetadata.class);

            assertEquals(sampleWorkflowLaunch.getLaunch().getPipelineId(), actual.getPipelineId());
            assertEquals(null, actual.getWorkspaceId());
            assertEquals(sampleDescribeWorkflow.getWorkflow().getOwnerId(), actual.getUserId());
            assertEquals(sampleDescribeWorkflow.getLabels(), actual.getLabels());
        }

        // workflow-load.json
        {
            WorkflowLoad actual = fromJSON(wfLoadJsonContent.get(), WorkflowLoad.class);
            assertEquals(sampleWorkflowProgress.getProgress().getWorkflowProgress(), actual);
        }

        // workflow-launch.json
        {
            LaunchDbDto actual = fromJSON(wfLaunchJsonContent.get(), LaunchDbDto.class);
            assertEquals(sampleLaunch.getLaunch(), actual);
        }

        // workflow-metrics.json
        {
            List<WorkflowMetrics> actual = new JSON()
                    .getContext(List.class)
                    .readValue(wfMetricsJsonContent.get(), new TypeReference<List<WorkflowMetrics>>(){});
            assertEquals(sampleWorkflowMetrics.getMetrics(), actual);
        }

        // workflow-tasks.json
        {
            List<Task> actual = new JSON()
                    .getContext(List.class)
                    .readValue(wfTasksJsonContent.get(), new TypeReference<List<Task>>(){});

            List<Task> expected = sampleTaskList.getTasks()
                    .stream()
                    .map(DescribeTaskResponse::getTask)
                    .toList();

            assertEquals(expected, actual);
        }

        // nextflow.log
        {
            String actual = new String(nxfLogJsonContent.get(), StandardCharsets.UTF_8);
            String expect = new String(sampleWorkflowLog, StandardCharsets.UTF_8);
            assertEquals(expect, actual);
        }

    }

    private static <T> T fromJSON(byte[] json, Class<T> clazz) throws JsonProcessingException {
        return JsonHelper.parseJson(new String(json, StandardCharsets.UTF_8), clazz);
    }

}