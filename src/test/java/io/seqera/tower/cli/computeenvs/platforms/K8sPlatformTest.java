/*
 * Copyright 2021-2026, Seqera.
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

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class K8sPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "k8s-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"description\":null,\"discriminator\":\"k8s\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T13:03:27Z\",\"lastUpdated\":\"2021-09-08T13:03:27Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody(
                        JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"platform\":\"k8s-platform\",\"config\":{\"workDir\":\"/workdir\",\"server\":\"k8s.mydomain.net\",\"sslCert\":\"ssl_cert\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\"}}}")
                ),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "k8s", "-n", "k8s", "--work-dir", "/workdir", "-s", "k8s.mydomain.net", "--namespace", "nf", "--ssl-cert", tempFile("ssl_cert", "", ".crt"), "--head-account", "head", "--storage-claim", "nf");
        assertOutput(format, out, new ComputeEnvAdded("k8s-platform", "isnEDBLvHDAIteOEF44ow", "k8s", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "k8s-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"description\":null,\"discriminator\":\"k8s\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T13:03:27Z\",\"lastUpdated\":\"2021-09-08T13:03:27Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody(
                        JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"platform\":\"k8s-platform\",\"config\":{\"workDir\":\"/workdir\",\"server\":\"k8s.mydomain.net\",\"sslCert\":\"ssl_cert\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\",\"storageMountPath\":\"/workdir\"}}}")
                ),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "k8s", "-n", "k8s", "--work-dir", "/workdir", "-s", "k8s.mydomain.net", "--namespace", "nf", "--ssl-cert", tempFile("ssl_cert", "", ".crt"), "--head-account", "head", "--storage-claim", "nf", "--storage-mount", "/workdir");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("k8s-platform", "isnEDBLvHDAIteOEF44ow", "k8s", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithStaging(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "k8s-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"description\":null,\"discriminator\":\"k8s\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T13:03:27Z\",\"lastUpdated\":\"2021-09-08T13:03:27Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody(
                        JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"platform\":\"k8s-platform\",\"config\":{\"workDir\":\"/workdir\",\"preRunScript\":\"pre_run_me\",\"postRunScript\":\"post_run_me\",\"server\":\"k8s.mydomain.net\",\"sslCert\":\"ssl_cert\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\"}}}")
                ),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "k8s", "-n", "k8s", "--work-dir", "/workdir", "-s", "k8s.mydomain.net", "--namespace", "nf", "--ssl-cert", tempFile("ssl_cert", "", ".crt"), "--head-account", "head", "--storage-claim", "nf", "--pre-run", tempFile("pre_run_me", "pre", "sh"), "--post-run", tempFile("post_run_me", "post", "sh"));

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("k8s-platform", "isnEDBLvHDAIteOEF44ow", "k8s", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }


}
