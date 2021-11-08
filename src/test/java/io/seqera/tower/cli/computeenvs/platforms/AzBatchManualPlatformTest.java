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


class AzBatchManualPlatformTest extends BaseCmdTest {

    @Test
    void testCreate(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "azure-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"57Ic6reczFn78H1DTaaXkp\",\"name\":\"azure\",\"description\":null,\"discriminator\":\"azure\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-07T13:50:21Z\",\"lastUpdated\":\"2021-09-07T13:50:21Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"azure-manual\",\"platform\":\"azure-batch\",\"config\":{\"workDir\":\"az://nextflow-ci/jordeu\",\"region\":\"europe\",\"headPool\":\"tower_pool\"},\"credentialsId\":\"57Ic6reczFn78H1DTaaXkp\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "azure-batch", "manual", "-n", "azure-manual", "-l", "europe", "--work-dir", "az://nextflow-ci/jordeu", "--compute-pool-name=tower_pool");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("azure-batch", "azure-manual", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateWithAdvancedOptions(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "azure-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"57Ic6reczFn78H1DTaaXkp\",\"name\":\"azure\",\"description\":null,\"discriminator\":\"azure\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-07T13:50:21Z\",\"lastUpdated\":\"2021-09-07T13:50:21Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"azure-manual\",\"platform\":\"azure-batch\",\"config\":{\"workDir\":\"az://nextflow-ci/jordeu\",\"region\":\"europe\",\"headPool\":\"tower_pool\",\"tokenDuration\":\"24\"},\"credentialsId\":\"57Ic6reczFn78H1DTaaXkp\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "create", "azure-batch", "manual", "-n", "azure-manual", "-l", "europe", "--work-dir", "az://nextflow-ci/jordeu", "--compute-pool-name=tower_pool", "--token-duration=24");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvCreated("azure-batch", "azure-manual", USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
