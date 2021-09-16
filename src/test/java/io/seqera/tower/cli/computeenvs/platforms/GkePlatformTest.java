package io.seqera.tower.cli.computeenvs.platforms;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.responses.ComputeEnvCreated;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class GkePlatformTest extends BaseCmdTest {

    @Test
    void testCreate(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "gke-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"gke\",\"platform\":\"gke-platform\",\"config\":{\"workDir\":\"/workdir\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\",\"region\":\"europe\",\"clusterName\":\"tower\"},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "gke", "-n", "gke", "-w", "/workdir", "-r", "europe", "--cluster-name", "tower", "--namespace", "nf", "--head-account", "head", "--storage-claim", "nf");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("gke-platform", "gke", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithAdvancedOptions(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "gke-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"gke\",\"platform\":\"gke-platform\",\"config\":{\"workDir\":\"/workdir\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\",\"storageMountPath\":\"/workdir\",\"region\":\"europe\",\"clusterName\":\"tower\"},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "gke", "-n", "gke", "-w", "/workdir", "-r", "europe", "--cluster-name", "tower", "--namespace", "nf", "--head-account", "head", "--storage-claim", "nf", "--storage-mount=/workdir");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("gke-platform", "gke", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
