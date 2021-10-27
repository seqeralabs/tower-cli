package io.seqera.tower.cli.runs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.RunCanceled;
import io.seqera.tower.cli.responses.RunDeleted;
import io.seqera.tower.cli.responses.RunList;
import io.seqera.tower.cli.responses.RunSubmited;
import io.seqera.tower.cli.responses.RunView;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListWorkflowsResponseListWorkflowsElement;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

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

class RunsCmdTest extends BaseCmdTest {

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
    void testListWithOffset(MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow")
                        .withQueryStringParameter("offset", "1")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_list")).withContentType(MediaType.APPLICATION_JSON)
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
        )).toString()), out.stdOut);
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
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListWithConflictingPageable(MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list", "--page", "1", "--offset", "0", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --page or --offset as pagination parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testListWithConflictingSizeable(MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workflow")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workflow_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "runs", "list", "--page", "1", "--no-max", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --no-max or --max as pagination size parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
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

        Launch launch = parseJson("{\n" +
                "    \"id\": \"5SCyEXKrCqFoGzOXGpesr5\",\n" +
                "    \"computeEnv\": {\n" +
                "      \"id\": \"3xkkzYH2nbD3nZjrzKm0oR\",\n" +
                "      \"name\": \"ce-aws-144996268157965\",\n" +
                "      \"description\": null,\n" +
                "      \"platform\": \"aws-batch\",\n" +
                "      \"config\": {\n" +
                "        \"region\": \"eu-west-1\",\n" +
                "        \"computeQueue\": \"TowerForge-3xkkzYH2nbD3nZjrzKm0oR-work\",\n" +
                "        \"computeJobRole\": null,\n" +
                "        \"headQueue\": \"TowerForge-3xkkzYH2nbD3nZjrzKm0oR-head\",\n" +
                "        \"headJobRole\": null,\n" +
                "        \"cliPath\": \"/home/ec2-user/miniconda/bin/aws\",\n" +
                "        \"volumes\": [],\n" +
                "        \"workDir\": \"s3://nextflow-ci/julio\",\n" +
                "        \"preRunScript\": null,\n" +
                "        \"postRunScript\": null,\n" +
                "        \"headJobCpus\": null,\n" +
                "        \"headJobMemoryMb\": null,\n" +
                "        \"forge\": {\n" +
                "          \"type\": \"SPOT\",\n" +
                "          \"minCpus\": 0,\n" +
                "          \"maxCpus\": 1,\n" +
                "          \"gpuEnabled\": false,\n" +
                "          \"ebsAutoScale\": true,\n" +
                "          \"instanceTypes\": [],\n" +
                "          \"allocStrategy\": null,\n" +
                "          \"imageId\": null,\n" +
                "          \"vpcId\": null,\n" +
                "          \"subnets\": [],\n" +
                "          \"securityGroups\": [],\n" +
                "          \"fsxMount\": null,\n" +
                "          \"fsxName\": null,\n" +
                "          \"fsxSize\": null,\n" +
                "          \"disposeOnDeletion\": true,\n" +
                "          \"ec2KeyPair\": null,\n" +
                "          \"allowBuckets\": [],\n" +
                "          \"ebsBlockSize\": null,\n" +
                "          \"fusionEnabled\": false,\n" +
                "          \"bidPercentage\": null,\n" +
                "          \"efsCreate\": false,\n" +
                "          \"efsId\": null,\n" +
                "          \"efsMount\": null\n" +
                "        },\n" +
                "        \"forgedResources\": [\n" +
                "          {\n" +
                "            \"IamRole\": \"arn:aws:iam::195996028523:role/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-ServiceRole\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"IamRole\": \"arn:aws:iam::195996028523:role/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-FleetRole\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"IamInstanceProfile\": \"arn:aws:iam::195996028523:instance-profile/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-InstanceRole\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"Ec2LaunchTemplate\": \"TowerForge-3xkkzYH2nbD3nZjrzKm0oR\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchEnv\": \"arn:aws:batch:eu-west-1:195996028523:compute-environment/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-head\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchQueue\": \"arn:aws:batch:eu-west-1:195996028523:job-queue/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-head\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchEnv\": \"arn:aws:batch:eu-west-1:195996028523:compute-environment/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-work\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchQueue\": \"arn:aws:batch:eu-west-1:195996028523:job-queue/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-work\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"platform\": \"aws-batch\"\n" +
                "      },\n" +
                "      \"dateCreated\": \"2021-09-21T12:56:19Z\",\n" +
                "      \"lastUpdated\": \"2021-09-21T12:57:03Z\",\n" +
                "      \"lastUsed\": \"2021-09-22T12:51:18Z\",\n" +
                "      \"deleted\": null,\n" +
                "      \"status\": \"AVAILABLE\",\n" +
                "      \"message\": null,\n" +
                "      \"primary\": null,\n" +
                "      \"credentialsId\": \"33Qc0pYMGcVImw6zc2xpLn\"\n" +
                "    },\n" +
                "    \"pipeline\": \"https://github.com/grananda/nextflow-hello\",\n" +
                "    \"workDir\": \"s3://nextflow-ci/julio\",\n" +
                "    \"revision\": \"main\",\n" +
                "    \"sessionId\": \"722eae42-f9e7-4631-9e9b-d77ed473071e\",\n" +
                "    \"configProfiles\": [],\n" +
                "    \"configText\": null,\n" +
                "    \"paramsText\": null,\n" +
                "    \"preRunScript\": null,\n" +
                "    \"postRunScript\": null,\n" +
                "    \"mainScript\": null,\n" +
                "    \"entryName\": null,\n" +
                "    \"schemaName\": null,\n" +
                "    \"resume\": false,\n" +
                "    \"pullLatest\": false,\n" +
                "    \"stubRun\": false,\n" +
                "    \"resumeDir\": \"s3://nextflow-ci/julio\",\n" +
                "    \"resumeCommitId\": null,\n" +
                "    \"dateCreated\": \"2021-09-22T11:43:46Z\"\n" +
                "  }", Launch.class);

        assertEquals("", out.stdErr);
        assertEquals(StringUtils.chop(new RunView("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME, workflow, workflowLoad, launch.getComputeEnv()).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testViewAsJson(MockServerClient mock) throws JsonProcessingException {
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

        Launch launch = parseJson("{\n" +
                "    \"id\": \"5SCyEXKrCqFoGzOXGpesr5\",\n" +
                "    \"computeEnv\": {\n" +
                "      \"id\": \"3xkkzYH2nbD3nZjrzKm0oR\",\n" +
                "      \"name\": \"ce-aws-144996268157965\",\n" +
                "      \"description\": null,\n" +
                "      \"platform\": \"aws-batch\",\n" +
                "      \"config\": {\n" +
                "        \"region\": \"eu-west-1\",\n" +
                "        \"computeQueue\": \"TowerForge-3xkkzYH2nbD3nZjrzKm0oR-work\",\n" +
                "        \"computeJobRole\": null,\n" +
                "        \"headQueue\": \"TowerForge-3xkkzYH2nbD3nZjrzKm0oR-head\",\n" +
                "        \"headJobRole\": null,\n" +
                "        \"cliPath\": \"/home/ec2-user/miniconda/bin/aws\",\n" +
                "        \"volumes\": [],\n" +
                "        \"workDir\": \"s3://nextflow-ci/julio\",\n" +
                "        \"preRunScript\": null,\n" +
                "        \"postRunScript\": null,\n" +
                "        \"headJobCpus\": null,\n" +
                "        \"headJobMemoryMb\": null,\n" +
                "        \"forge\": {\n" +
                "          \"type\": \"SPOT\",\n" +
                "          \"minCpus\": 0,\n" +
                "          \"maxCpus\": 1,\n" +
                "          \"gpuEnabled\": false,\n" +
                "          \"ebsAutoScale\": true,\n" +
                "          \"instanceTypes\": [],\n" +
                "          \"allocStrategy\": null,\n" +
                "          \"imageId\": null,\n" +
                "          \"vpcId\": null,\n" +
                "          \"subnets\": [],\n" +
                "          \"securityGroups\": [],\n" +
                "          \"fsxMount\": null,\n" +
                "          \"fsxName\": null,\n" +
                "          \"fsxSize\": null,\n" +
                "          \"disposeOnDeletion\": true,\n" +
                "          \"ec2KeyPair\": null,\n" +
                "          \"allowBuckets\": [],\n" +
                "          \"ebsBlockSize\": null,\n" +
                "          \"fusionEnabled\": false,\n" +
                "          \"bidPercentage\": null,\n" +
                "          \"efsCreate\": false,\n" +
                "          \"efsId\": null,\n" +
                "          \"efsMount\": null\n" +
                "        },\n" +
                "        \"forgedResources\": [\n" +
                "          {\n" +
                "            \"IamRole\": \"arn:aws:iam::195996028523:role/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-ServiceRole\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"IamRole\": \"arn:aws:iam::195996028523:role/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-FleetRole\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"IamInstanceProfile\": \"arn:aws:iam::195996028523:instance-profile/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-InstanceRole\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"Ec2LaunchTemplate\": \"TowerForge-3xkkzYH2nbD3nZjrzKm0oR\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchEnv\": \"arn:aws:batch:eu-west-1:195996028523:compute-environment/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-head\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchQueue\": \"arn:aws:batch:eu-west-1:195996028523:job-queue/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-head\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchEnv\": \"arn:aws:batch:eu-west-1:195996028523:compute-environment/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-work\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"BatchQueue\": \"arn:aws:batch:eu-west-1:195996028523:job-queue/TowerForge-3xkkzYH2nbD3nZjrzKm0oR-work\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"platform\": \"aws-batch\"\n" +
                "      },\n" +
                "      \"dateCreated\": \"2021-09-21T12:56:19Z\",\n" +
                "      \"lastUpdated\": \"2021-09-21T12:57:03Z\",\n" +
                "      \"lastUsed\": \"2021-09-22T12:51:18Z\",\n" +
                "      \"deleted\": null,\n" +
                "      \"status\": \"AVAILABLE\",\n" +
                "      \"message\": null,\n" +
                "      \"primary\": null,\n" +
                "      \"credentialsId\": \"33Qc0pYMGcVImw6zc2xpLn\"\n" +
                "    },\n" +
                "    \"pipeline\": \"https://github.com/grananda/nextflow-hello\",\n" +
                "    \"workDir\": \"s3://nextflow-ci/julio\",\n" +
                "    \"revision\": \"main\",\n" +
                "    \"sessionId\": \"722eae42-f9e7-4631-9e9b-d77ed473071e\",\n" +
                "    \"configProfiles\": [],\n" +
                "    \"configText\": null,\n" +
                "    \"paramsText\": null,\n" +
                "    \"preRunScript\": null,\n" +
                "    \"postRunScript\": null,\n" +
                "    \"mainScript\": null,\n" +
                "    \"entryName\": null,\n" +
                "    \"schemaName\": null,\n" +
                "    \"resume\": false,\n" +
                "    \"pullLatest\": false,\n" +
                "    \"stubRun\": false,\n" +
                "    \"resumeDir\": \"s3://nextflow-ci/julio\",\n" +
                "    \"resumeCommitId\": null,\n" +
                "    \"dateCreated\": \"2021-09-22T11:43:46Z\"\n" +
                "  }", Launch.class);

        assertEquals("", out.stdErr);
        assertEquals(prettyJson(new RunView("5dAZoXrcmZXRO4", USER_WORKSPACE_NAME, workflow, workflowLoad, launch.getComputeEnv()).getJSON()), out.stdOut);
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
    void testRelaunch(MockServerClient mock) {
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


        ExecOut out = exec(mock, "runs", "relaunch", "-i", "5UVJlhfUAHTuAP");


        assertEquals("", out.stdErr);
        assertEquals(new RunSubmited("35aLiS0bIM5efd", String.format("%s/user/jordi/watch/35aLiS0bIM5efd", url(mock)), "user").toString(), out.stdOut);
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
            assertEquals(errorMessage(out.app, new ShowUsageException(out.app.spec.subcommands().get("runs").getCommandSpec())), out.stdErr);
            assertEquals("", out.stdOut);
            assertEquals(-1, out.exitCode);
        }
    }
}