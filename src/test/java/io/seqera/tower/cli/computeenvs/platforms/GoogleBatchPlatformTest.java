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
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class GoogleBatchPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws IOException {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"workDir\":\"gs://workdir\"},\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe");
        assertOutput(format, out, new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) throws IOException {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"usePrivateAddress\":true,\"waveEnabled\":true,\"fusion2Enabled\":true}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--fusion-v2", "--wave", "--use-private-address");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithInstanceTemplates(MockServerClient mock) throws IOException {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"headJobInstanceTemplate\":\"projects/my-project/global/instanceTemplates/head-template\",\"computeJobsInstanceTemplate\":\"projects/my-project/global/instanceTemplates/compute-template\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--head-job-template", "projects/my-project/global/instanceTemplates/head-template", "--compute-job-template", "projects/my-project/global/instanceTemplates/compute-template");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithOnlyHeadJobTemplate(MockServerClient mock) throws IOException {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"headJobInstanceTemplate\":\"projects/my-project/global/instanceTemplates/head-template\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--head-job-template", "projects/my-project/global/instanceTemplates/head-template");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddWithOnlyComputeJobTemplate(MockServerClient mock) throws IOException {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "google-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs")
                        .withBody(JsonBody.json("{\"computeEnv\":{\"credentialsId\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"platform\":\"google-batch\",\"config\":{\"location\":\"europe\",\"workDir\":\"gs://workdir\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"computeJobsInstanceTemplate\":\"projects/my-project/global/instanceTemplates/compute-template\"}}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "google-batch", "-n", "google", "--work-dir", "gs://workdir", "-l", "europe", "--compute-job-template", "projects/my-project/global/instanceTemplates/compute-template");
        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("google-batch", "isnEDBLvHDAIteOEF44ow", "google", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
