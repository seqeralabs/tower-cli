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

package io.seqera.tower.cli.workspaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import io.seqera.tower.cli.responses.workspaces.WorkspaceAdded;
import io.seqera.tower.cli.responses.workspaces.WorkspaceDeleted;
import io.seqera.tower.cli.responses.workspaces.WorkspaceList;
import io.seqera.tower.cli.responses.workspaces.WorkspaceUpdated;
import io.seqera.tower.cli.responses.workspaces.WorkspaceView;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.Visibility;
import org.junit.jupiter.api.Test;
import io.seqera.tower.model.Workspace;
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

public class WorkspacesCmdTest extends BaseCmdTest {

    @Test
    void testList(MockServerClient mock) throws JsonProcessingException {
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

        ExecOut out = exec(mock, "workspaces", "list");


        assertEquals("", out.stdErr);
        assertEquals(chop(new WorkspaceList("jordi", Arrays.asList(parseJson(" {\n" +
                        "      \"orgId\": 27736513644467,\n" +
                        "      \"orgName\": \"organization1\",\n" +
                        "      \"orgLogoUrl\": null,\n" +
                        "      \"workspaceId\": 75887156211589,\n" +
                        "      \"workspaceName\": \"workspace1\"\n" +
                        "    }", OrgAndWorkspaceDbDto.class),
                parseJson("{\n" +
                        "      \"orgId\": 37736513644467,\n" +
                        "      \"orgName\": \"organization2\",\n" +
                        "      \"orgLogoUrl\": null,\n" +
                        "      \"workspaceId\": 75887156211590,\n" +
                        "      \"workspaceName\": \"workspace2\"\n" +
                        "    }", OrgAndWorkspaceDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListByOrganization(MockServerClient mock) throws JsonProcessingException {
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

        ExecOut out = exec(mock, "workspaces", "list", "-o", "organization1");


        assertEquals("", out.stdErr);
        assertEquals(chop(new WorkspaceList("jordi", List.of(parseJson(" {\n" +
                "      \"orgId\": 27736513644467,\n" +
                "      \"orgName\": \"organization1\",\n" +
                "      \"orgLogoUrl\": null,\n" +
                "      \"workspaceId\": 75887156211589,\n" +
                "      \"workspaceName\": \"workspace1\"\n" +
                "    }", OrgAndWorkspaceDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListEmpty(MockServerClient mock) {
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

        ExecOut out = exec(mock, "workspaces", "list");


        assertEquals("", out.stdErr);
        assertEquals(chop(new WorkspaceList("jordi", List.of()).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testDeleteById(MockServerClient mock) {
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
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "workspaces", "delete", "-w", "75887156211589");

        assertEquals("", out.stdErr);
        assertEquals(new WorkspaceDeleted("workspace1", "organization1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testDeleteNotFound(MockServerClient mock) {
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
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "workspaces", "delete", "-w", "7588715621158");

        assertEquals(errorMessage(out.app, new WorkspaceNotFoundException(7588715621158L)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testViewById(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "workspaces", "view", "-w", "75887156211589");

        assertEquals("", out.stdErr);
        assertEquals(chop(new WorkspaceView(parseJson("{\n" +
                "    \"id\": 75887156211589,\n" +
                "    \"name\": \"workspace1\",\n" +
                "    \"fullName\": \"workspace 1\",\n" +
                "    \"description\": \"Workspace description\",\n" +
                "    \"visibility\": \"PRIVATE\",\n" +
                "    \"dateCreated\": \"2021-09-21T12:54:03Z\",\n" +
                "    \"lastUpdated\": \"2021-09-21T12:54:03Z\"\n" +
                "  }", Workspace.class)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testViewNotFound(MockServerClient mock) throws JsonProcessingException {
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

        ExecOut out = exec(mock, "workspaces", "view", "-w", "7588715621158");

        assertEquals(errorMessage(out.app, new WorkspaceNotFoundException(7588715621158L)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testAdd(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/validate").withQueryStringParameter("name", "wspNew"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        mock.when(
                request().withMethod("POST").withPath("/orgs/27736513644467/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_add_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "workspaces", "add", "-n", "wspNew", "-o", "organization1", "-f", "wsp-new", "-d", "workspace description");

        assertEquals("", out.stdErr);
        assertEquals(new WorkspaceAdded("wspNew", "organization1", Visibility.PRIVATE).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAddOrganizationNotFound(MockServerClient mock) {
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

        ExecOut out = exec(mock, "workspaces", "add", "-n", "wspNew", "-o", "organization1", "-f", "wsp-new", "-d", "workspace description");

        assertEquals(errorMessage(out.app, new OrganizationNotFoundException("organization1")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void updateTestById(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_update_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "workspaces", "update", "-w", "75887156211589", "-f", "wsp-new", "-d", "workspace description");

        assertEquals("", out.stdErr);
        assertEquals(new WorkspaceUpdated("workspace1", "organization1", Visibility.PRIVATE).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void updateTestWorkspaceNotFound(MockServerClient mock) {
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

        ExecOut out = exec(mock, "workspaces", "update", "-w", "7588715621158", "-f", "wsp-new", "-d", "workspace description");

        assertEquals(errorMessage(out.app, new WorkspaceNotFoundException(7588715621158L)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void leaveWorkspaceAsParticipantById(MockServerClient mock) {
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
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589/participants"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "workspaces", "leave", "-w", "75887156211589");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantLeft("workspace1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
