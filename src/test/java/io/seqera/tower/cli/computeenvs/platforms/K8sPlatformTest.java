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

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class K8sPlatformTest extends BaseCmdTest {

    @Test
    void testCreate(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "k8s-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"description\":null,\"discriminator\":\"k8s\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T13:03:27Z\",\"lastUpdated\":\"2021-09-08T13:03:27Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"k8s\",\"platform\":\"k8s-platform\",\"config\":{\"workDir\":\"/workdir\",\"server\":\"k8s.mydomain.net\",\"sslCert\":\"ssl_cert\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\"},\"credentialsId\":\"2iEjbUUbqbuOaQEx03OxyH\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "k8s", "-n", "k8s", "-w", "/workdir", "-s", "k8s.mydomain.net", "--namespace", "nf", "--ssl-cert", tempFile("ssl_cert", "", ".crt"), "--head-account", "head", "--storage-claim", "nf");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("k8s-platform", "k8s", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithAdvancedOptions(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "k8s-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"description\":null,\"discriminator\":\"k8s\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T13:03:27Z\",\"lastUpdated\":\"2021-09-08T13:03:27Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"k8s\",\"platform\":\"k8s-platform\",\"config\":{\"workDir\":\"/workdir\",\"server\":\"k8s.mydomain.net\",\"sslCert\":\"ssl_cert\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\",\"storageMountPath\":\"/workdir\"},\"credentialsId\":\"2iEjbUUbqbuOaQEx03OxyH\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "k8s", "-n", "k8s", "-w", "/workdir", "-s", "k8s.mydomain.net", "--namespace", "nf", "--ssl-cert", tempFile("ssl_cert", "", ".crt"), "--head-account", "head", "--storage-claim", "nf", "--storage-mount", "/workdir");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("k8s-platform", "k8s", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithStaging(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "k8s-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2iEjbUUbqbuOaQEx03OxyH\",\"name\":\"k8s\",\"description\":null,\"discriminator\":\"k8s\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-08T13:03:27Z\",\"lastUpdated\":\"2021-09-08T13:03:27Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"k8s\",\"platform\":\"k8s-platform\",\"config\":{\"workDir\":\"/workdir\",\"preRunScript\":\"pre_run_me\",\"postRunScript\":\"post_run_me\",\"server\":\"k8s.mydomain.net\",\"sslCert\":\"ssl_cert\",\"namespace\":\"nf\",\"headServiceAccount\":\"head\",\"storageClaimName\":\"nf\"},\"credentialsId\":\"2iEjbUUbqbuOaQEx03OxyH\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "k8s", "-n", "k8s", "-w", "/workdir", "-s", "k8s.mydomain.net", "--namespace", "nf", "--ssl-cert", tempFile("ssl_cert", "", ".crt"), "--head-account", "head", "--storage-claim", "nf", "--pre-run", tempFile("pre_run_me", "pre", "sh"), "--post-run", tempFile("post_run_me", "post", "sh"));

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("k8s-platform", "k8s", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }


}
