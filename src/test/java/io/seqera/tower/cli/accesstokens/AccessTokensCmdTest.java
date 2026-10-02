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


package io.seqera.tower.cli.accesstokens;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.accesstokens.AccessTokenAdded;
import io.seqera.tower.cli.responses.accesstokens.AccessTokenDeleted;
import io.seqera.tower.cli.responses.accesstokens.AccessTokensList;
import io.seqera.tower.model.AccessToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class AccessTokensCmdTest extends BaseCmdTest {

    private static final String TOKEN_1 = "{\"id\":21,\"name\":\"ci\",\"dateCreated\":\"2026-09-01T10:00:00Z\",\"lastUsed\":\"2026-09-02T10:00:00Z\"}";
    private static final String LEGACY_TOKEN = "{\"id\":22,\"name\":\"legacy\",\"basicAuth\":\"dG93ZXI6c2VjcmV0\",\"dateCreated\":\"2021-01-01T10:00:00Z\",\"lastUsed\":null}";
    private static final String LIST = "{\"tokens\":[" + TOKEN_1 + "," + LEGACY_TOKEN + "]}";

    private void mockList(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/tokens"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(LIST).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws IOException {
        mockList(mock);

        ExecOut out = exec(format, mock, "access-tokens", "list");

        assertOutput(format, out, new AccessTokensList(List.of(
                parseJson(TOKEN_1, AccessToken.class),
                parseJson("{\"id\":22,\"name\":\"legacy\",\"dateCreated\":\"2021-01-01T10:00:00Z\"}", AccessToken.class)
        )));
        assertFalse(out.stdOut.contains("dG93ZXI6c2VjcmV0"), "basicAuth must never be printed");
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/tokens").withBody(json("{\"name\":\"ci\"}")), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"accessKey\":\"eyJ0aWQiOjIxfQ.secret\",\"token\":" + TOKEN_1 + "}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "access-tokens", "add", "-n", "ci");

        assertOutput(format, out, new AccessTokenAdded(21L, "ci", "eyJ0aWQiOjIxfQ.secret"));
        assertTrue(out.stdOut.contains("eyJ0aWQiOjIxfQ.secret"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteByName(OutputType format, MockServerClient mock) {
        mockList(mock);
        mock.when(
                request().withMethod("DELETE").withPath("/tokens/22"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "access-tokens", "delete", "-n", "legacy");

        assertOutput(format, out, new AccessTokenDeleted(22L, "legacy"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteById(OutputType format, MockServerClient mock) {
        mockList(mock);
        mock.when(
                request().withMethod("DELETE").withPath("/tokens/21"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "access-tokens", "delete", "-i", "21");

        assertOutput(format, out, new AccessTokenDeleted(21L, "ci"));
    }

    @Test
    void testDeleteUnknownId(MockServerClient mock) {
        mockList(mock);

        ExecOut out = exec(mock, "access-tokens", "delete", "-i", "99");

        assertEquals(1, out.exitCode);
        assertTrue(out.stdErr.contains("Unknown access token '99'"), out.stdErr);
    }
}
