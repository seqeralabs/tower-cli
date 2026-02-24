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
import org.mockserver.model.MediaType;

import java.io.IOException;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class GoogleCloudPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
        mock.reset();

        // given
        mock.when(
                request()
                        .withMethod("GET")
                        .withPath("/credentials")
                        .withQueryStringParameter("platformId", "google-cloud"),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}")
        );

        mock.when(
                request()
                        .withMethod("POST")
                        .withPath("/compute-envs")
                        .withBody(json("""
                                {
                                    "computeEnv": {
                                        "name": "my-google-cloud-ce",
                                        "platform": "google-cloud",
                                        "config": {
                                            "workDir": "gs://my-bucket",
                                            "region": "us-central1",
                                            "zone": "us-central1-a",
                                            "fusion2Enabled": true,
                                            "waveEnabled": true
                                        },
                                        "credentialsId": "6XfOhoztUq6de3Dw3X9LSb"
                                    }
                                }""")),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}")
        );

        // when
        var out = exec(format, mock, "compute-envs", "add", "google-cloud",
                "--name", "my-google-cloud-ce",
                "--work-dir", "gs://my-bucket",
                "--region", "us-central1",
                "--zone", "us-central1-a"
        );

        // then
        var expected = new ComputeEnvAdded("google-cloud", "isnEDBLvHDAIteOEF44ow", "my-google-cloud-ce", null, USER_WORKSPACE_NAME);
        assertOutput(format, out, expected);
    }

    @Test
    void testAddWithAdvancedOptions(MockServerClient mock) throws IOException {
        mock.reset();

        // given
        mock.when(
                request()
                        .withMethod("GET")
                        .withPath("/credentials")
                        .withQueryStringParameter("platformId", "google-cloud"),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"credentials\":[{\"id\":\"6XfOhoztUq6de3Dw3X9LSb\",\"name\":\"google\",\"description\":null,\"discriminator\":\"google\",\"baseUrl\":null,\"category\":null,\"deleted\":null,\"lastUsed\":\"2021-09-08T18:20:46Z\",\"dateCreated\":\"2021-09-08T12:57:04Z\",\"lastUpdated\":\"2021-09-08T12:57:04Z\"}]}")
        );

        mock.when(
                request()
                        .withMethod("POST")
                        .withPath("/compute-envs")
                        .withBody(json("""
                                {
                                    "computeEnv": {
                                        "name": "my-google-cloud-advanced",
                                        "platform": "google-cloud",
                                        "config": {
                                            "workDir": "gs://my-bucket",
                                            "region": "europe-west1",
                                            "zone": "europe-west1-b",
                                            "instanceType": "n2-standard-4",
                                            "imageId": "projects/my-project/global/images/my-image",
                                            "fusion2Enabled": true,
                                            "waveEnabled": true,
                                            "arm64Enabled": true,
                                            "bootDiskSizeGb": 100
                                        },
                                        "credentialsId": "6XfOhoztUq6de3Dw3X9LSb"
                                    }
                                }""")),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}")
        );

        // when
        ExecOut out = exec(mock, "compute-envs", "add", "google-cloud",
                "-n", "my-google-cloud-advanced",
                "--work-dir", "gs://my-bucket",
                "-r", "europe-west1",
                "-z", "europe-west1-b",
                "--instance-type", "n2-standard-4",
                "--image-id", "projects/my-project/global/images/my-image",
                "--arm64",
                "--boot-disk-size", "100"
        );

        // then
        var expected = new ComputeEnvAdded("google-cloud", "isnEDBLvHDAIteOEF44ow", "my-google-cloud-advanced", null, USER_WORKSPACE_NAME);
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(expected.toString(), out.stdOut);
    }
}
