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

package io.seqera.tower.cli.secrets;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.secrets.SecretAdded;
import io.seqera.tower.cli.responses.secrets.SecretDeleted;
import io.seqera.tower.cli.responses.secrets.SecretUpdated;
import io.seqera.tower.cli.responses.secrets.SecretView;
import io.seqera.tower.cli.responses.secrets.SecretsList;
import io.seqera.tower.model.PipelineSecret;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.List;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class SecretsCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {

        // Mock API
        mock.when(
                request().withMethod("GET").withPath("/pipeline-secrets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"pipelineSecrets\":[{\"id\":5002114781502,\"name\":\"name01\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T12:42:21Z\",\"lastUpdated\":\"2022-10-25T12:42:21Z\"},{\"id\":171740984431657,\"name\":\"name02\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T13:21:15Z\",\"lastUpdated\":\"2022-10-25T13:21:15Z\"}],\"totalSize\":2}").withContentType(MediaType.APPLICATION_JSON)
        );

        // Run the command
        ExecOut out = exec(format, mock, "secrets", "list");

        assertOutput(format, out, new SecretsList(USER_WORKSPACE_NAME, List.of(
                parseJson("{\"id\":5002114781502,\"dateCreated\":\"2022-10-25T12:42:21Z\",\"lastUpdated\":\"2022-10-25T12:42:21Z\"}", PipelineSecret.class).name("name01"),
                parseJson("{\"id\":171740984431657,\"dateCreated\":\"2022-10-25T13:21:15Z\",\"lastUpdated\":\"2022-10-25T13:21:15Z\"}", PipelineSecret.class).name("name02")
        )));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {

        // Mock API
        mock.when(
                request().withMethod("POST").withPath("/pipeline-secrets").withBody("{\"name\":\"name03\",\"value\":\"value03\"}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"secretId\":164410928765888}").withContentType(MediaType.APPLICATION_JSON)
        );

        // Run command
        ExecOut out = exec(format, mock, "secrets", "add", "-n", "name03", "-v", "value03");

        // Assert output
        assertOutput(format, out, new SecretAdded(USER_WORKSPACE_NAME, 164410928765888L, "name03"));

    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddWithOverride(OutputType format, MockServerClient mock) {

        // Mock API
        mock.when(
                request().withMethod("GET").withPath("/pipeline-secrets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"pipelineSecrets\":[{\"id\":5002114781502,\"name\":\"name01\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T12:42:21Z\",\"lastUpdated\":\"2022-10-25T12:42:21Z\"},{\"id\":171740984431657,\"name\":\"name02\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T13:21:15Z\",\"lastUpdated\":\"2022-10-25T13:21:15Z\"},{\"id\":164410928765888,\"name\":\"name03\",\"lastUsed\":null,\"dateCreated\":\"2022-10-26T07:05:17Z\",\"lastUpdated\":\"2022-10-26T07:05:17Z\"}],\"totalSize\":3}").withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("DELETE").withPath("/pipeline-secrets/164410928765888"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );
        mock.when(
                request().withMethod("POST").withPath("/pipeline-secrets").withBody("{\"name\":\"name03\",\"value\":\"value03\"}"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"secretId\":164410928765888}").withContentType(MediaType.APPLICATION_JSON)
        );

        // Run command
        ExecOut out = exec(format, mock, "secrets", "add", "--overwrite", "-n", "name03", "-v", "value03");

        // Assert output
        assertOutput(format, out, new SecretAdded(USER_WORKSPACE_NAME, 164410928765888L, "name03"));

    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/pipeline-secrets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"pipelineSecrets\":[{\"id\":5002114781502,\"name\":\"name01\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T12:42:21Z\",\"lastUpdated\":\"2022-10-25T12:42:21Z\"},{\"id\":171740984431657,\"name\":\"name02\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T13:21:15Z\",\"lastUpdated\":\"2022-10-25T13:21:15Z\"},{\"id\":164410928765888,\"name\":\"name03\",\"lastUsed\":null,\"dateCreated\":\"2022-10-26T07:05:17Z\",\"lastUpdated\":\"2022-10-26T07:05:17Z\"}],\"totalSize\":3}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/pipeline-secrets/164410928765888"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "secrets", "delete", "-n", "name03");
        assertOutput(format, out, new SecretDeleted(
                parseJson("{\"dateCreated\":\"2022-10-26T07:05:17Z\",\"lastUpdated\":\"2022-10-26T07:05:17Z\"}", PipelineSecret.class).name("name03").id(164410928765888L),
                USER_WORKSPACE_NAME
        ));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/pipeline-secrets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"pipelineSecrets\":[{\"id\":5002114781502,\"name\":\"name01\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T12:42:21Z\",\"lastUpdated\":\"2022-10-25T12:42:21Z\"},{\"id\":171740984431657,\"name\":\"name02\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T13:21:15Z\",\"lastUpdated\":\"2022-10-25T13:21:15Z\"}],\"totalSize\":2}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "secrets", "view", "-n", "name02");
        assertOutput(format, out, new SecretView(
                USER_WORKSPACE_NAME,
                parseJson("{\"lastUsed\":null,\"dateCreated\":\"2022-10-25T13:21:15Z\",\"lastUpdated\":\"2022-10-25T13:21:15Z\"}", PipelineSecret.class).name("name02").id(171740984431657L)
        ));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdate(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/pipeline-secrets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"pipelineSecrets\":[{\"id\":5002114781502,\"name\":\"name01\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T12:42:21Z\",\"lastUpdated\":\"2022-10-25T12:42:21Z\"},{\"id\":171740984431657,\"name\":\"name02\",\"lastUsed\":null,\"dateCreated\":\"2022-10-25T13:21:15Z\",\"lastUpdated\":\"2022-10-25T13:21:15Z\"}],\"totalSize\":2}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/pipeline-secrets/171740984431657").withBody("{\"value\":\"updateValue\"}"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "secrets", "update", "-n", "name02", "-v", "updateValue");
        assertOutput(format, out, new SecretUpdated(USER_WORKSPACE_NAME, "name02"));
    }
}
