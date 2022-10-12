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
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvAdded;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class AltairPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "altair-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"jdeu\",\"description\":null,\"discriminator\":\"ssh\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-06T08:53:51Z\",\"dateCreated\":\"2021-09-06T06:54:53Z\",\"lastUpdated\":\"2021-09-06T06:54:53Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"altair\",\"platform\":\"altair-platform\",\"config\":{\"workDir\":\"/home/jordeu/nf\",\"userName\":\"jordi\",\"hostName\":\"ssh.mydomain.net\",\"headQueue\":\"normal\"},\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "altair", "-n", "altair", "--work-dir", "/home/jordeu/nf", "-u", "jordi", "-H", "ssh.mydomain.net", "-q", "normal");
        assertOutput(format, out, new ComputeEnvAdded("altair-platform", "isnEDBLvHDAIteOEF44ow", "altair", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "altair-platform"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"jdeu\",\"description\":null,\"discriminator\":\"ssh\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-06T08:53:51Z\",\"dateCreated\":\"2021-09-06T06:54:53Z\",\"lastUpdated\":\"2021-09-06T06:54:53Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"name\":\"altair\",\"platform\":\"altair-platform\",\"config\":{\"workDir\":\"/home/jordeu/nf\",\"userName\":\"jordi\",\"hostName\":\"ssh.mydomain.net\",\"headQueue\":\"normal\",\"maxQueueSize\":200},\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\"}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "altair", "-n", "altair", "--work-dir", "/home/jordeu/nf", "-u", "jordi", "-H", "ssh.mydomain.net", "-q", "normal", "--max-queue-size=200");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("altair-platform", "isnEDBLvHDAIteOEF44ow", "altair", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }


}
