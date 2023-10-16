/*
 * Copyright 2023, Seqera.
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

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


class AzBatchManualPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "azure-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(JsonBody.json("{\"credentials\":[{\"id\":\"57Ic6reczFn78H1DTaaXkp\",\"name\":\"azure\",\"description\":null,\"discriminator\":\"azure\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-07T13:50:21Z\",\"lastUpdated\":\"2021-09-07T13:50:21Z\"}]}")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"azure-manual\",\"platform\":\"azure-batch\",\"config\":{\"workDir\":\"az://nextflow-ci/jordeu\",\"region\":\"europe\",\"fusion2Enabled\":false,\"waveEnabled\":false,\"headPool\":\"tower_pool\"},\"credentialsId\":\"57Ic6reczFn78H1DTaaXkp\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "add", "azure-batch", "manual", "-n", "azure-manual", "-l", "europe", "--work-dir", "az://nextflow-ci/jordeu", "--compute-pool-name=tower_pool");
        assertOutput(format, out, new ComputeEnvAdded("azure-batch", "isnEDBLvHDAIteOEF44ow", "azure-manual", null, USER_WORKSPACE_NAME));
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/credentials").withQueryStringParameter("platformId", "azure-batch"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\":[{\"id\":\"57Ic6reczFn78H1DTaaXkp\",\"name\":\"azure\",\"description\":null,\"discriminator\":\"azure\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":null,\"dateCreated\":\"2021-09-07T13:50:21Z\",\"lastUpdated\":\"2021-09-07T13:50:21Z\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/compute-envs").withBody(JsonBody.json("{\"computeEnv\":{\"name\":\"azure-manual\",\"platform\":\"azure-batch\",\"config\":{\"workDir\":\"az://nextflow-ci/jordeu\",\"region\":\"europe\",\"fusion2Enabled\":true,\"waveEnabled\":true,\"headPool\":\"tower_pool\",\"tokenDuration\":\"24\"},\"credentialsId\":\"57Ic6reczFn78H1DTaaXkp\"}}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "compute-envs", "add", "azure-batch", "manual", "-n", "azure-manual", "-l", "europe", "--work-dir", "az://nextflow-ci/jordeu", "--fusion-v2", "--wave", "--compute-pool-name=tower_pool", "--token-duration=24");

        assertEquals("", out.stdErr);
        assertEquals(new ComputeEnvAdded("azure-batch", "isnEDBLvHDAIteOEF44ow", "azure-manual", null, USER_WORKSPACE_NAME).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
