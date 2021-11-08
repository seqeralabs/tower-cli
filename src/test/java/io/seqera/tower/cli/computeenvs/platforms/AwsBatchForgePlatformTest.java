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
import io.seqera.tower.cli.responses.ComputeEnvCreated;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class AwsBatchForgePlatformTest extends BaseCmdTest {

    @Test
    void testCreate(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fusionEnabled\":true}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("aws-batch", "demo", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithEFS(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fusionEnabled\":false,\"efsCreate\":true}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--create-efs");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("aws-batch", "demo", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithFSX(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"workDir\":\"/workdir\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"fsxSize\":1200,\"disposeOnDeletion\":true,\"fusionEnabled\":false}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "/workdir", "--max-cpus=123", "--fsx-size=1200");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("aws-batch", "demo", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithAdvanced(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"cliPath\":\"/bin/aws\",\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":8,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"allowBuckets\":[\"bkt1\",\"bkt2\"],\"fusionEnabled\":true}},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "aws-batch", "forge", "-n", "demo", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion", "--cli-path=/bin/aws", "--min-cpus=8", "--allow-buckets=bkt1,bkt2");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("aws-batch", "demo", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
