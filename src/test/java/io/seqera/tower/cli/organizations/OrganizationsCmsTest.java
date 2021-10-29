/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.organizations;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.organizations.OrganizationsCreated;
import io.seqera.tower.cli.responses.organizations.OrganizationsDeleted;
import io.seqera.tower.cli.responses.organizations.OrganizationsList;
import io.seqera.tower.cli.responses.organizations.OrganizationsUpdated;
import io.seqera.tower.cli.responses.organizations.OrganizationsView;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.OrganizationDbDto;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Arrays;
import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class OrganizationsCmsTest extends BaseCmdTest {

    @Test
    void testListOrganization(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "organizations", "list");

        assertEquals("", out.stdErr);
        assertEquals(chop(new OrganizationsList("jordi", Arrays.asList(parseJson(" {\n" +
                        "      \"orgId\": 27736513644467,\n" +
                        "      \"orgName\": \"organization1\",\n" +
                        "      \"orgLogoUrl\": null,\n" +
                        "      \"workspaceId\": null,\n" +
                        "      \"workspaceName\": null\n" +
                        "    }", OrgAndWorkspaceDbDto.class),
                parseJson(" {\n" +
                        "      \"orgId\": 37736513644467,\n" +
                        "      \"orgName\": \"organization2\",\n" +
                        "      \"orgLogoUrl\": null,\n" +
                        "      \"workspaceId\": null,\n" +
                        "      \"workspaceName\": null\n" +
                        "    }", OrgAndWorkspaceDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListOrganizationEmpty(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"orgsAndWorkspaces\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "organizations", "list");

        assertEquals("", out.stdErr);
        assertEquals(chop(new OrganizationsList("jordi", List.of()).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testViewOrganization(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("organizations/organizations_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "organizations", "view", "-n", "organization1");

        assertEquals("", out.stdErr);
        assertEquals(chop(new OrganizationsView(parseJson("{\n" +
                "    \"orgId\": 27736513644467,\n" +
                "    \"name\": \"organization1\",\n" +
                "    \"fullName\": \"organization1\",\n" +
                "    \"description\": null,\n" +
                "    \"location\": null,\n" +
                "    \"website\": null,\n" +
                "    \"logoId\": null,\n" +
                "    \"logoUrl\": null,\n" +
                "    \"memberId\": null,\n" +
                "    \"memberRole\": null\n" +
                "  }", OrganizationDbDto.class)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testViewOrganizationNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "organizations", "view", "-n", "organization11");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new OrganizationNotFoundException("organization11")), out.stdErr);
    }

    @Test
    void testDeleteOrganization(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "organizations", "delete", "-n", "organization1");

        assertEquals("", out.stdErr);
        assertEquals(new OrganizationsDeleted("organization1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testDeleteOrganizationNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "organizations", "delete", "-n", "organization11");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new OrganizationNotFoundException("organization11")), out.stdErr);
    }

    @Test
    void testDeleteOrganizationError(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "organizations", "delete", "-n", "organization1");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
        assertEquals(errorMessage(out.app, new TowerException("Organization organization1 could not be deleted")), out.stdErr);
    }

    @Test
    void testCreateOrganization(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("POST").withPath("/orgs"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("organizations/organizations_create_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "organizations", "create", "-n", "sample-organization", "-f", "sample organization");

        assertEquals("", out.stdErr);
        assertEquals(new OrganizationsCreated(parseJson("{\n" +
                "    \"orgId\": 275484385882108,\n" +
                "    \"name\": \"sample-organization\",\n" +
                "    \"fullName\": \"sample organization\",\n" +
                "    \"description\": \"sample organization description\",\n" +
                "    \"location\": \"office\",\n" +
                "    \"website\": \"http://www.seqera.io\",\n" +
                "    \"logoId\": null,\n" +
                "    \"logoUrl\": null,\n" +
                "    \"memberId\": null,\n" +
                "    \"memberRole\": null\n" +
                "  }", OrganizationDbDto.class)).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateOrganizationError(MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/orgs"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "organizations", "create", "-n", "sample-organization", "-f", "sample organization");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testUpdateOrganization(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("organizations/organizations_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "organizations", "update", "-n", "organization1", "-f", "sample organization");

        assertEquals("", out.stdErr);
        assertEquals(new OrganizationsUpdated("organization1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testUpdateOrganizationError(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467"), exactly(1)
        ).respond(
                response().withStatusCode(500)
        );

        ExecOut out = exec(mock, "organizations", "update", "-n", "organization1", "-f", "sample organization");

        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }
}
