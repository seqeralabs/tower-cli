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

package io.seqera.tower.cli.computeenvs.platforms;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
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
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":true,\"waveEnabled\":false,\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fusionEnabled\":false,\"fargateHeadEnabled\":false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion-v2");
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
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fusionEnabled\":false,\"efsCreate\":true,\"fargateHeadEnabled\":false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
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
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"/workdir\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"fsxSize\":1200,\"disposeOnDeletion\":true,\"fusionEnabled\":false, \"fargateHeadEnabled\": false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
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
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":true,\"waveEnabled\":false,\"cliPath\":\"/bin/aws\",\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":8,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"allowBuckets\":[\"bkt1\",\"bkt2\"],\"fusionEnabled\":false,\"fargateHeadEnabled\": false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion-v2", "--cli-path=/bin/aws", "--min-cpus=8", "--allow-buckets=bkt1,bkt2");

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
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"s3://nextflow-ci/jordeu\",\"environment\":[{\"name\":\"HEAD\",\"value\":\"value1\",\"head\":true,\"compute\":false},{\"name\":\"COMPUTE\",\"value\":\"value2\",\"head\":false,\"compute\":true},{\"name\":\"BOTH\",\"value\":\"value3\",\"head\":true,\"compute\":true},{\"name\":\"HEAD\",\"value\":\"value4\",\"head\":true,\"compute\":false}],\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fusionEnabled\":false, \"fargateHeadEnabled\": false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "-e", "HEAD=value1", "-e", "compute:COMPUTE=value2", "-e", "both:BOTH=value3", "-e", "head:HEAD=value4");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "demo", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
