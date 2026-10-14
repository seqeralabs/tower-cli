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

package io.seqera.tower.cli.organizations;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.organizations.IdpGroupAdded;
import io.seqera.tower.cli.responses.organizations.IdpGroupDeleted;
import io.seqera.tower.cli.responses.organizations.IdpGroupsList;
import io.seqera.tower.cli.responses.organizations.ScimConfigView;
import io.seqera.tower.cli.responses.organizations.ScimTokenCreated;
import io.seqera.tower.cli.responses.organizations.ScimTokenRevoked;
import io.seqera.tower.model.CreateScimTokenResponse;
import io.seqera.tower.model.DescribeScimConfigResponse;
import io.seqera.tower.model.IdpGroupEntry;
import io.seqera.tower.model.ListIdpGroupsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.MediaType;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class OrganizationIdentityCmdTest extends BaseCmdTest {

    private static final String ORG_PATH = "/orgs/27736513644467";

    private static final String IDP_GROUPS = """
            {"groups": [{"id": 5, "displayName": "eng-admins", "source": "SCIM"}, {"id": 6, "displayName": "eng", "source": "MANUAL"}]}
            """;

    private static final String SCIM_TOKEN = """
            {"endpointUrl": "https://tower.example/api/scim/v2", "maskedToken": "sqscim_ab...", "token": "sqscim_abcdef"}
            """;

    @BeforeEach
    void mockUserWorkspaces(MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath("/user-info"))
                .respond(response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON));
        mock.when(request().withMethod("GET").withPath("/user/1264/workspaces"))
                .respond(response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testIdpGroupsList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(request().withMethod("GET").withPath(ORG_PATH + "/idp-groups"), exactly(1))
                .respond(response().withStatusCode(200).withBody(IDP_GROUPS).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "organizations", "idp-groups", "list", "-o", "organization1");

        assertOutput(format, out, new IdpGroupsList("organization1", parseJson(IDP_GROUPS, ListIdpGroupsResponse.class).getGroups()));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testIdpGroupsAdd(OutputType format, MockServerClient mock) throws JsonProcessingException {
        String created = "{\"id\": 7, \"displayName\": \"data-team\", \"source\": \"MANUAL\"}";
        mock.when(
                request().withMethod("POST").withPath(ORG_PATH + "/idp-groups")
                        .withBody(json("{\"displayName\": \"data-team\"}", MatchType.STRICT)), exactly(1)
        ).respond(response().withStatusCode(200).withBody(created).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "organizations", "idp-groups", "add", "-o", "organization1", "-n", "data-team");

        assertOutput(format, out, new IdpGroupAdded("organization1", parseJson(created, IdpGroupEntry.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testIdpGroupsDeleteByName(OutputType format, MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath(ORG_PATH + "/idp-groups"), exactly(1))
                .respond(response().withStatusCode(200).withBody(IDP_GROUPS).withContentType(MediaType.APPLICATION_JSON));
        mock.when(request().withMethod("DELETE").withPath(ORG_PATH + "/idp-groups/6"), exactly(1))
                .respond(response().withStatusCode(204));

        ExecOut out = exec(format, mock, "organizations", "idp-groups", "delete", "-o", "organization1", "-n", "eng");

        assertOutput(format, out, new IdpGroupDeleted("organization1", "eng"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testIdpGroupsDeleteById(OutputType format, MockServerClient mock) {
        mock.when(request().withMethod("DELETE").withPath(ORG_PATH + "/idp-groups/6"), exactly(1))
                .respond(response().withStatusCode(204));

        ExecOut out = exec(format, mock, "organizations", "idp-groups", "delete", "-o", "organization1", "-i", "6");

        assertOutput(format, out, new IdpGroupDeleted("organization1", "6"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testScimView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        String body = """
                {"endpointUrl": "https://tower.example/api/scim/v2", "groupCount": 3, "hasActiveToken": true, "maskedToken": "sqscim_ab...",
                 "ssoActive": true, "tokenCreatedAt": "2026-09-01T10:00:00Z", "tokenLastUsed": null}
                """;
        mock.when(request().withMethod("GET").withPath(ORG_PATH + "/scim/config"), exactly(1))
                .respond(response().withStatusCode(200).withBody(body).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "organizations", "scim", "view", "-o", "organization1");

        assertOutput(format, out, new ScimConfigView("organization1", parseJson(body, DescribeScimConfigResponse.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testScimCreateToken(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(request().withMethod("POST").withPath(ORG_PATH + "/scim/token"), exactly(1))
                .respond(response().withStatusCode(200).withBody(SCIM_TOKEN).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "organizations", "scim", "create-token", "-o", "organization1");

        assertOutput(format, out, new ScimTokenCreated("organization1", parseJson(SCIM_TOKEN, CreateScimTokenResponse.class), false));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testScimRotateToken(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(request().withMethod("POST").withPath(ORG_PATH + "/scim/token/rotate"), exactly(1))
                .respond(response().withStatusCode(200).withBody(SCIM_TOKEN).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "organizations", "scim", "rotate-token", "-o", "organization1");

        assertOutput(format, out, new ScimTokenCreated("organization1", parseJson(SCIM_TOKEN, CreateScimTokenResponse.class), true));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testScimRevokeToken(OutputType format, MockServerClient mock) {
        mock.when(request().withMethod("DELETE").withPath(ORG_PATH + "/scim/token"), exactly(1))
                .respond(response().withStatusCode(204));

        ExecOut out = exec(format, mock, "organizations", "scim", "revoke-token", "-o", "organization1");

        assertOutput(format, out, new ScimTokenRevoked("organization1"));
    }
}
