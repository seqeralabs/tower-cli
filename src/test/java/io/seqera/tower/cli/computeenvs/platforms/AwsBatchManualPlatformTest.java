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


class AwsBatchManualPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(JsonBody.json("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"manual\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"computeQueue\":\"TowerForge-isnEDBLvHDAIteOEF44ow-work\",\"headQueue\":\"TowerForge-isnEDBLvHDAIteOEF44ow-head\",\"workDir\":\"s3://nextflow-ci/jordeu\"},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "aws-batch", "manual", "-n", "manual", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--head-queue", "TowerForge-isnEDBLvHDAIteOEF44ow-head", "--compute-queue", "TowerForge-isnEDBLvHDAIteOEF44ow-work");
        assertOutput(format, out, new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "manual", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddAdvanceOptions(MockServerClient mock) {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "aws-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"discriminator\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"manual\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"fusion2Enabled\":true,\"waveEnabled\":true,\"nvnmeStorageEnabled\":true,\"computeQueue\":\"TowerForge-isnEDBLvHDAIteOEF44ow-work\",\"executionRole\":\"execution-arn\",\"headQueue\":\"TowerForge-isnEDBLvHDAIteOEF44ow-head\",\"cliPath\":\"/bin/aws\",\"workDir\":\"s3://nextflow-ci/jordeu\"},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "aws-batch", "manual", "-n", "manual", "-r", "eu-west-1", "--work-dir", "s3://nextflow-ci/jordeu", "--fusion-v2", "--wave", "--fast-storage", "--head-queue", "TowerForge-isnEDBLvHDAIteOEF44ow-head", "--compute-queue", "TowerForge-isnEDBLvHDAIteOEF44ow-work", "--cli-path=/bin/aws", "--batch-execution-role", "execution-arn");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("aws-batch", "isnEDBLvHDAIteOEF44ow", "manual", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
