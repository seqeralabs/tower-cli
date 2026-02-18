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

package io.seqera.tower.cli.pipelineschemas;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.pipelineschemas.PipelineSchemasAdded;
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

public class PipelineSchemaCmdTest extends BaseCmdTest {

    private static final String SCHEMA_CONTENT = "{\"type\":\"object\",\"properties\":{\"input\":{\"type\":\"string\"}}}";

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddWithUserWorkspace(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("POST").withPath("/pipeline-schemas"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("pipelineschemas/create_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "pipeline-schemas", "add", "-c", tempFile(SCHEMA_CONTENT, "schema", ".json"));
        assertOutput(format, out, new PipelineSchemasAdded(USER_WORKSPACE_NAME, 98765L));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddWithWorkspace(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("POST").withPath("/pipeline-schemas")
                        .withQueryStringParameter("workspaceId", "75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("pipelineschemas/create_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "pipeline-schemas", "add", "-c", tempFile(SCHEMA_CONTENT, "schema", ".json"), "-w", "75887156211589");
        assertOutput(format, out, new PipelineSchemasAdded("[organization1 / workspace1]", 98765L));
    }

    @Test
    void testAddApiError401(MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("POST").withPath("/pipeline-schemas"), exactly(1)
        ).respond(
                response().withStatusCode(401)
        );

        ExecOut out = exec(mock, "pipeline-schemas", "add", "-c", tempFile(SCHEMA_CONTENT, "schema", ".json"));
        assertEquals(1, out.exitCode);
        assertEquals("", out.stdOut);
        assertEquals(errorMessage(out.app, new ApiException(401, "Unauthorized")), out.stdErr);
    }

    @Test
    void testAddFileNotFound(MockServerClient mock) {
        ExecOut out = exec(mock, "pipeline-schemas", "add", "-c", "/nonexistent/path/schema.json");
        assertEquals(1, out.exitCode);
        assertEquals("", out.stdOut);
        assertEquals(errorMessage(out.app, new java.nio.file.NoSuchFileException(java.nio.file.Path.of("/nonexistent/path/schema.json").toString())), out.stdErr);
    }
}
