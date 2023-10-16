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

package io.seqera.tower.cli.computeenvs.platforms;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvAdded;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class AwsBatchForgePlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":true,\"waveEnabled\":true,\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fargateHeadEnabled\":false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion-v2", "--wave");
        assertOutput(format, out, new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "demo", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddWithEFS(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"efsCreate\":true,\"fargateHeadEnabled\":false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--create-efs");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "demo", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithFSX(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"/workdir\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"fsxSize\":1200,\"disposeOnDeletion\":true,\"fargateHeadEnabled\": false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "/workdir", "--max-cpus=123", "--fsx-size=1200");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "demo", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithAdvanced(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":true,\"waveEnabled\":true,\"nvnmeStorageEnabled\":true,\"cliPath\":\"/bin/aws\",\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":8,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"allowBuckets\":[\"bkt1\",\"bkt2\"],\"fargateHeadEnabled\": false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion-v2", "--wave", "--fast-storage", "--cli-path=/bin/aws", "--min-cpus=8", "--allow-buckets=bkt1,bkt2");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "demo", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithEnvVars(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"s3://nextflow-ci/jordeu\",\"environment\":[{\"name\":\"HEAD\",\"value\":\"value1\",\"head\":true,\"compute\":false},{\"name\":\"COMPUTE\",\"value\":\"value2\",\"head\":false,\"compute\":true},{\"name\":\"BOTH\",\"value\":\"value3\",\"head\":true,\"compute\":true},{\"name\":\"HEAD\",\"value\":\"value4\",\"head\":true,\"compute\":false}],\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fargateHeadEnabled\": false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "-e", "HEAD=value1", "-e", "compute:COMPUTE=value2", "-e", "both:BOTH=value3", "-e", "head:HEAD=value4");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "demo", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithDeprecated(MockServerClient mock) {

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion");

        assertEquals(errorMessage(out.app, new TowerException("Fusion v1 is deprecated, please use '--fusion-v2' instead")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

}
