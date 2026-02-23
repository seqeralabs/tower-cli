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

public class SeqeraComputePlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
        mock.reset();

        // given
        mock.when(
                request()
                        .withMethod("POST")
                        .withPath("/compute-envs")
                        .withBody(json("""
                                {
                                    "computeEnv": {
                                        "name": "my-compute-env",
                                        "platform": "seqeracompute-platform",
                                        "config": {
                                            "region": "eu-west-1"
                                        }
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
        var out = exec(format, mock, "compute-envs", "add", "seqera-compute",
                "--name", "my-compute-env",
                "--region", "eu-west-1"
        );

        // then
        var expected = new ComputeEnvAdded("seqeracompute-platform", "isnEDBLvHDAIteOEF44ow", "my-compute-env", null, USER_WORKSPACE_NAME);
        assertOutput(format, out, expected);
    }

    @Test
    void testAddWithOptions(MockServerClient mock) throws IOException {
        mock.reset();

        // given
        mock.when(
                request()
                        .withMethod("POST")
                        .withPath("/compute-envs")
                        .withBody(json("""
                                {
                                    "computeEnv": {
                                        "name": "another-compute-env",
                                        "platform": "seqeracompute-platform",
                                        "config": {
                                            "region": "eu-west-2",
                                            "preRunScript": "pre_run_me",
                                            "postRunScript": "post_run_me",
                                            "environment": [
                                                { "name": "KEY1", "value": "value1", "head": true, "compute": false},
                                                { "name": "KEY2", "value": "value2", "head": true, "compute": true}
                                            ],
                                            "nextflowConfig": "nextflow_config"
                                        }
                                    }
                                }
                                """)),
                exactly(1)
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"computeEnvId\":\"isnEDBLvHDAIteOEF44ow\"}")
        );

        // when
        ExecOut out = exec(mock, "compute-envs", "add", "seqera-compute",
                "-n", "another-compute-env",
                "-r", "eu-west-2",
                "--work-dir", "my-work-dir",
                "--nextflow-config", tempFile("nextflow_config", "nextflow", "config"),
                "-e", "head:KEY1=value1", "-e", "both:KEY2=value2",
                "--pre-run", tempFile("pre_run_me", "pre", "sh"),
                "--post-run", tempFile("post_run_me", "post", "sh")
        );

        // then
        var expected = new ComputeEnvAdded("seqeracompute-platform", "isnEDBLvHDAIteOEF44ow", "another-compute-env", null, USER_WORKSPACE_NAME);
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(expected.toString(), out.stdOut);
    }
}
