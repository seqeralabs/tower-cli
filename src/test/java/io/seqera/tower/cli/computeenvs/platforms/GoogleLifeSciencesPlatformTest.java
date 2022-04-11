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
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvAdded;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class GoogleLifeSciencesPlatformTest extends BaseCmdTest {

    @Test
    void testAdd(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-lifesciences"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"google\",\"platform\":\"google-lifesciences\",\"config\":{\"region\":\"europe\",\"workDir\":\"gs://workdir\"},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-ls", "-n", "google", "--work-dir", "gs://workdir", "-r", "europe");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-lifesciences", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-lifesciences"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"google\",\"platform\":\"google-lifesciences\",\"config\":{\"region\":\"europe\",\"workDir\":\"gs://workdir\",\"usePrivateAddress\":true},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-ls", "-n", "google", "--work-dir", "gs://workdir", "-r", "europe", "--use-private-address");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-lifesciences", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithFileStore(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-lifesciences"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"google\",\"platform\":\"google-lifesciences\",\"config\":{\"region\":\"europe\",\"workDir\":\"gs://workdir\",\"nfsTarget\":\"1.2.3.4:/my_share_name\"},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-ls", "-n", "google", "--work-dir", "gs://workdir", "-r", "europe", "--nfs-target=1.2.3.4:/my_share_name");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-lifesciences", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
