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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.nio.file.Path;

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
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"altair\",\"platform\":\"altair-platform\",\"config\":{\"userName\":\"jordi\",\"hostName\":\"ssh.mydomain.net\",\"workDir\":\"/home/jordeu/nf\",\"headQueue\":\"normal\"}}}"), exactly(1)
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
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"altair\",\"platform\":\"altair-platform\",\"config\":{\"userName\":\"jordi\",\"hostName\":\"ssh.mydomain.net\",\"maxQueueSize\":200,\"workDir\":\"/home/jordeu/nf\",\"headQueue\":\"normal\"}}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "altair", "-n", "altair", "--work-dir", "/home/jordeu/nf", "-u", "jordi", "-H", "ssh.mydomain.net", "-q", "normal", "--max-queue-size=200");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("altair-platform", "isnEDBLvHDAIteOEF44ow", "altair", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithEnvVars(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "altair-platform"),exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"jdeu\",\"description\":null,\"discriminator\":\"ssh\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-06T08:53:51Z\",\"dateCreated\":\"2021-09-06T06:54:53Z\",\"lastUpdated\":\"2021-09-06T06:54:53Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"altair\",\"platform\":\"altair-platform\",\"config\":{\"environment\":[{\"name\":\"HEAD\",\"value\":\"value1\",\"head\":true,\"compute\":false},{\"name\":\"COMPUTE\",\"value\":\"value2\",\"head\":false,\"compute\":true},{\"name\":\"BOTH\",\"value\":\"value3\",\"head\":true,\"compute\":true},{\"name\":\"HEAD\",\"value\":\"value4\",\"head\":true,\"compute\":false}],\"workDir\":\"/home/jordeu/nf\",\"headQueue\":\"normal\"}}}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "altair", "-n", "altair", "--work-dir", "/home/jordeu/nf", "-q", "normal", "-e", "HEAD=value1", "-e", "compute:COMPUTE=value2", "-e", "both:BOTH=value3", "-e", "head:HEAD=value4");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("altair-platform", "isnEDBLvHDAIteOEF44ow", "altair", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithNextflowConfigFile(MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "altair-platform"),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"jdeu\",\"description\":null,\"discriminator\":\"ssh\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-06T08:53:51Z\",\"dateCreated\":\"2021-09-06T06:54:53Z\",\"lastUpdated\":\"2021-09-06T06:54:53Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody("{\"computeEnv\":{\"credentialsId\":\"2ba2oekqeTEBzwSDgXg7xf\",\"name\":\"altair\",\"platform\":\"altair-platform\",\"config\":{\"userName\":\"jordi\",\"hostName\":\"ssh.mydomain.net\",\"workDir\":\"/home/jordeu/nf\",\"nextflowConfig\":\"nextflow_config\",\"headQueue\":\"normal\"}}}"),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "altair", "-n", "altair", "--work-dir", "/home/jordeu/nf", "-u", "jordi", "-H", "ssh.mydomain.net", "-q", "normal", "--nextflow-config", tempFile("nextflow_config", "nextflow", "config"));

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("altair-platform", "isnEDBLvHDAIteOEF44ow", "altair", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
