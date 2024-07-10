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
import org.mockserver.model.MediaType;

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class EksPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "eks-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"eks\",\"platform\":\"eks-platform\",\"config\":{\"workDir\":\"/workdir\",\"region\":\"europe\",\"clusterName\":\"tower\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\"}}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "eks", "-n", "eks", "--work-dir", "/workdir", "-r", "europe", "--cluster-name", "tower", "--namespace", "nf", "--head-account", "head", "--storage-claim", "nf");
        assertOutput(format, out, new ComputeEnvAdded("eks-platform", "isnEDBLvHDAIteOEF44ow", "eks", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "eks-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"eks\",\"platform\":\"eks-platform\",\"config\":{\"region\":\"europe\",\"clusterName\":\"tower\",\"workDir\":\"/workdir\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\",\"storageMountPath\":\"/workdir\"},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "eks", "-n", "eks", "--work-dir", "/workdir", "-r", "europe", "--cluster-name", "tower", "--namespace", "nf", "--head-account", "head", "--storage-claim", "nf", "--storage-mount=/workdir");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("eks-platform", "isnEDBLvHDAIteOEF44ow", "eks", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
