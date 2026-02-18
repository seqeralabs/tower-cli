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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvView;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.ComputeEnvStatus;
import io.seqera.tower.model.GoogleLifeSciencesConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Collections;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class GoogleLifeSciencesPlatformTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/compute-envs/6vjjZYhrfgvJZ9nCPgdXcJ"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("compute_env_view_gls")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "compute-envs", "view", "-i", "6vjjZYhrfgvJZ9nCPgdXcJ");

        GoogleLifeSciencesConfig config = parseJson("{\"discriminator\": \"google-lifesciences\"}", GoogleLifeSciencesConfig.class);
        config.workDir("gs://testing")
                .environment(Collections.emptyList());
        config.region("europe-west2")
                .zones(Collections.emptyList())
                .location("europe-west2")
                .preemptible(true)
                .sshDaemon(false)
                .usePrivateAddress(false);

        ComputeEnvResponseDto computeEnv = parseJson("{\"id\": \"6vjjZYhrfgvJZ9nCPgdXcJ\", \"dateCreated\": \"2021-09-08T11:19:24Z\", \"lastUpdated\": \"2021-09-08T11:20:08Z\"}", ComputeEnvResponseDto.class)
                .name("google-lifesciences-ce")
                .platform(ComputeEnvResponseDto.PlatformEnum.GOOGLE_LIFESCIENCES)
                .status(ComputeEnvStatus.AVAILABLE)
                .credentialsId("3s590teTD2wpCSxCNh03Am")
                .config(config);

        assertOutput(format, out, new ComputeEnvView("6vjjZYhrfgvJZ9nCPgdXcJ", USER_WORKSPACE_NAME,
                computeEnv,
                baseUserUrl(mock, USER_WORKSPACE_NAME)
        ));
    }
}

