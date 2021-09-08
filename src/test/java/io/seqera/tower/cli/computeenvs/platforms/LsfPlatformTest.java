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


class LsfPlatformTest extends BaseCmdTest {

    @Test
    void testCreate(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "lsf-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"jdeu\",\"description\":null,\"provider\":\"ssh\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-06T08:53:51Z\",\"dateCreated\":\"2021-09-06T06:54:53Z\",\"lastUpdated\":\"2021-09-06T06:54:53Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"lsf\",\"platform\":\"lsf-platform\",\"config\":{\"platform\":\"lsf-platform\",\"workDir\":\"/home/jordeu/nf\",\"userName\":\"jordi\",\"hostName\":\"ssh.mydomain.net\",\"headQueue\":\"normal\"},\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock , "compute-envs", "create", "lsf", "-n", "lsf", "-w", "/home/jordeu/nf", "-u", "jordi", "-H", "ssh.mydomain.net", "-q", "normal");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("lsf-platform", "lsf", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithAdvancedOptions(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "lsf-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"jdeu\",\"description\":null,\"provider\":\"ssh\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-06T08:53:51Z\",\"dateCreated\":\"2021-09-06T06:54:53Z\",\"lastUpdated\":\"2021-09-06T06:54:53Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"lsf\",\"platform\":\"lsf-platform\",\"config\":{\"platform\":\"lsf-platform\",\"workDir\":\"/home/jordeu/nf\",\"userName\":\"jordi\",\"hostName\":\"ssh.mydomain.net\",\"headQueue\":\"normal\",\"maxQueueSize\":200},\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "lsf", "-n", "lsf", "-w", "/home/jordeu/nf", "-u", "jordi", "-H", "ssh.mydomain.net", "-q", "normal", "--max-queue-size=200");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("lsf-platform", "lsf", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }


}
