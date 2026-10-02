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

package io.seqera.tower.cli.roles;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.roles.RoleAdded;
import io.seqera.tower.cli.responses.roles.RoleDeleted;
import io.seqera.tower.cli.responses.roles.RolePermissionsList;
import io.seqera.tower.cli.responses.roles.RoleUpdated;
import io.seqera.tower.cli.responses.roles.RoleView;
import io.seqera.tower.cli.responses.roles.RolesList;
import io.seqera.tower.model.DescribeRoleResponse;
import io.seqera.tower.model.ListRolePermissionsResponse;
import io.seqera.tower.model.ListRolesResponse;
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

class RolesCmdTest extends BaseCmdTest {

    private static final String ORG_ID = "27736513644467";

    private static final String ROLE = """
            {"role": {"name": "runner", "description": "Launch only", "isPredefined": false, "permissions": ["pipeline:launch", "workflow:read"]}}
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
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        String body = """
                {"roles": [
                  {"name": "owner", "description": "Full permissions", "isPredefined": true},
                  {"name": "runner", "description": "Launch only", "isPredefined": false}
                ], "totalSize": 2}
                """;
        mock.when(
                request().withMethod("GET").withPath("/roles")
                        .withQueryStringParameter("orgId", ORG_ID)
                        .withQueryStringParameter("type", "custom")
                        .withQueryStringParameter("name", "run")
                        .withQueryStringParameter("max", "100")
                        .withQueryStringParameter("offset", "0"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(body).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "roles", "list", "-o", "organization1", "-t", "custom", "-f", "run");

        assertOutput(format, out, new RolesList("organization1", parseJson(body, ListRolesResponse.class).getRoles(), null));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/roles/runner").withQueryStringParameter("orgId", ORG_ID), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(ROLE).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "roles", "view", "-o", "organization1", "-n", "runner");

        assertOutput(format, out, new RoleView("organization1", parseJson(ROLE, DescribeRoleResponse.class).getRole()));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/roles").withQueryStringParameter("orgId", ORG_ID)
                        .withBody(json("""
                                {"name": "runner", "description": "Launch only", "permissions": ["pipeline:launch", "workflow:read"]}
                                """, MatchType.STRICT)), exactly(1)
        ).respond(
                response().withStatusCode(201).withBody(ROLE).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "roles", "add", "-o", "organization1", "-n", "runner", "-d", "Launch only", "-p", "pipeline:launch,workflow:read");

        assertOutput(format, out, new RoleAdded("organization1", "runner"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateKeepsUnsetFields(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/roles/runner").withQueryStringParameter("orgId", ORG_ID), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(ROLE).withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("PUT").withPath("/roles/runner").withQueryStringParameter("orgId", ORG_ID)
                        .withBody(json("""
                                {"name": "launcher", "description": "Launch only", "permissions": ["pipeline:launch", "workflow:read"]}
                                """, MatchType.STRICT)), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "roles", "update", "-o", "organization1", "-n", "runner", "--new-name", "launcher");

        assertOutput(format, out, new RoleUpdated("organization1", "launcher"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateReplacesPermissions(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/roles/runner").withQueryStringParameter("orgId", ORG_ID), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(ROLE).withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("PUT").withPath("/roles/runner").withQueryStringParameter("orgId", ORG_ID)
                        .withBody(json("""
                                {"name": "runner", "description": "Read only", "permissions": ["workflow:read"]}
                                """, MatchType.STRICT)), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "roles", "update", "-o", "organization1", "-n", "runner", "-d", "Read only", "-p", "workflow:read");

        assertOutput(format, out, new RoleUpdated("organization1", "runner"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/roles/runner").withQueryStringParameter("orgId", ORG_ID), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "roles", "delete", "-o", "organization1", "-n", "runner");

        assertOutput(format, out, new RoleDeleted("organization1", "runner"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testPermissions(OutputType format, MockServerClient mock) throws JsonProcessingException {
        String body = """
                {"permissions": [{"name": "pipeline:launch", "category": "Pipelines"}, {"name": "workflow:read", "category": "Runs"}]}
                """;
        mock.when(
                request().withMethod("GET").withPath("/roles/permissions").withQueryStringParameter("orgId", ORG_ID), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(body).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "roles", "permissions", "-o", "organization1");

        assertOutput(format, out, new RolePermissionsList(parseJson(body, ListRolePermissionsResponse.class).getPermissions()));
    }
}
