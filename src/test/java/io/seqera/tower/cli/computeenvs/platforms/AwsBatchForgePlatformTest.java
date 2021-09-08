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
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6g0ER59L4ZoE5zpOmUP48D\",\"name\":\"aws\",\"description\":null,\"provider\":\"aws\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T11:09:58Z\",\"dateCreated\":\"2021-09-08T05:48:51Z\",\"lastUpdated\":\"2021-09-08T05:48:51Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"demo\",\"platform\":\"aws-batch\",\"config\":{\"region\":\"eu-west-1\",\"workDir\":\"s3://nextflow-ci/jordeu\",\"forge\":{\"type\":\"SPOT\",\"minCpus\":0,\"maxCpus\":123,\"gpuEnabled\":false,\"ebsAutoScale\":true,\"disposeOnDeletion\":true,\"fusionEnabled\":true},\"platform\":\"aws-batch\"},\"credentialsId\":\"6g0ER59L4ZoE5zpOmUP48D\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "aws", "-n", "demo", "-r", "eu-west-1", "-w", "s3://nextflow-ci/jordeu", "--max-cpus=123", "--fusion");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("aws-batch", "demo", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testUpdate(MockServerClient mock) {

    }
}
