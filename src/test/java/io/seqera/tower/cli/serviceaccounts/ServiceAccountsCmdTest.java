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

package io.seqera.tower.cli.serviceaccounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountAdded;
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountDeleted;
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountUpdated;
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountView;
import io.seqera.tower.cli.responses.serviceaccounts.ServiceAccountsList;
import io.seqera.tower.model.ServiceAccountDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.MediaType;

import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class ServiceAccountsCmdTest extends BaseCmdTest {

    private static final String ORG_PATH = "/orgs/27736513644467/service-accounts";

    private static final String SA_LIST = """
            {
              "serviceAccounts": [
                {"id": 11, "name": "ci-bot", "description": "CI pipelines", "createdAt": "2026-09-01T10:00:00Z", "memberId": 21},
                {"id": 12, "name": "nightly", "description": null, "createdAt": "2026-09-02T10:00:00Z", "memberId": 22}
              ],
              "totalSize": 2
            }
            """;

    private static final String CI_BOT = """
            {"id": 11, "name": "ci-bot", "description": "CI pipelines", "createdAt": "2026-09-01T10:00:00Z", "memberId": 21}
            """;

    @BeforeEach
    void mockUserWorkspaces(MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath("/user-info"))
                .respond(response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON));
        mock.when(request().withMethod("GET").withPath("/user/1264/workspaces"))
                .respond(response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON));
    }

    private static ServiceAccountDto ciBot() throws JsonProcessingException {
        return parseJson(CI_BOT, ServiceAccountDto.class);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath(ORG_PATH)
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "100"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(SA_LIST).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "service-accounts", "list", "-o", "organization1");

        assertOutput(format, out, new ServiceAccountsList("organization1", null,
                parseJson(SA_LIST, io.seqera.tower.model.ListServiceAccountsResponse.class).getServiceAccounts(),
                null));
    }

    @Test
    void testListWorkspaceFollowsPageToken(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/service-accounts")
                        .withQueryStringParameter("pageToken", "next-1"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("""
                        {"items": [{"id": 12, "name": "nightly", "createdAt": "2026-09-02T10:00:00Z"}], "nextPageToken": null}
                        """).withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/service-accounts")
                        .withQueryStringParameter("pageSize", "100"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("""
                        {"items": [{"id": 11, "name": "ci-bot", "description": "CI pipelines", "createdAt": "2026-09-01T10:00:00Z"}], "nextPageToken": "next-1"}
                        """).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(OutputType.json, mock, "service-accounts", "list", "-w", "75887156211589");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(true, out.stdOut.contains("\"workspaceName\" : \"workspace1\""), out.stdOut);
        assertEquals(true, out.stdOut.contains("\"name\" : \"ci-bot\""), out.stdOut);
        assertEquals(true, out.stdOut.contains("\"name\" : \"nightly\""), out.stdOut);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("POST").withPath(ORG_PATH)
                        .withBody(json("""
                                {"name": "ci-bot", "description": "CI pipelines"}
                                """, MatchType.STRICT)), exactly(1)
        ).respond(
                response().withStatusCode(201).withBody("{\"serviceAccount\": " + CI_BOT + "}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "service-accounts", "add", "-o", "organization1", "-n", "ci-bot", "-d", "CI pipelines");

        assertOutput(format, out, new ServiceAccountAdded("organization1", ciBot()));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testViewById(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath(ORG_PATH + "/11"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"serviceAccount\": " + CI_BOT + "}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "service-accounts", "view", "-o", "organization1", "-i", "11");

        assertOutput(format, out, new ServiceAccountView("organization1", ciBot()));
    }

    @Test
    void testViewByName(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath(ORG_PATH), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(SA_LIST).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(OutputType.console, mock, "service-accounts", "view", "-o", "organization1", "-n", "ci-bot");

        assertOutput(OutputType.console, out, new ServiceAccountView("organization1", ciBot()));
    }

    @Test
    void testViewByNameNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath(ORG_PATH), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(SA_LIST).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "service-accounts", "view", "-o", "organization1", "-n", "missing");

        assertEquals(errorMessage(out.app, new TowerException("Service account 'missing' not found in organization '27736513644467'")), out.stdErr);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateSendsOnlyChangedFields(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath(ORG_PATH + "/11"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"serviceAccount\": " + CI_BOT + "}").withContentType(MediaType.APPLICATION_JSON)
        );
        String updated = CI_BOT.replace("ci-bot", "ci-runner");
        mock.when(
                request().withMethod("PATCH").withPath(ORG_PATH + "/11")
                        .withBody(json("{\"name\": \"ci-runner\"}", MatchType.STRICT)), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"serviceAccount\": " + updated + "}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "service-accounts", "update", "-o", "organization1", "-i", "11", "--new-name", "ci-runner");

        assertOutput(format, out, new ServiceAccountUpdated("organization1", parseJson(updated, ServiceAccountDto.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteByName(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath(ORG_PATH), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(SA_LIST).withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("DELETE").withPath(ORG_PATH + "/11"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "service-accounts", "delete", "-o", "organization1", "-n", "ci-bot");

        assertOutput(format, out, new ServiceAccountDeleted("organization1", ciBot()));
    }
}
