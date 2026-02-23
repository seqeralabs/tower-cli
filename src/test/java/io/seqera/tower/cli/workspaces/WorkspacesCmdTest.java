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

package io.seqera.tower.cli.workspaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import io.seqera.tower.cli.responses.workspaces.WorkspaceAdded;
import io.seqera.tower.cli.responses.workspaces.WorkspaceDeleted;
import io.seqera.tower.cli.responses.workspaces.WorkspaceList;
import io.seqera.tower.cli.responses.workspaces.WorkspaceUpdated;
import io.seqera.tower.cli.responses.workspaces.WorkspaceView;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.Visibility;
import io.seqera.tower.model.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import java.util.Arrays;
import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class WorkspacesCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
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

        ExecOut out = exec(format, mock, "workspaces", "list");
        assertOutput(format, out, new WorkspaceList("jordi", Arrays.asList(parseJson(" {\n" +
                        "      \"orgId\": 27736513644467,\n" +
                        "      \"orgName\": \"organization1\",\n" +
                        "      \"orgLogoUrl\": null,\n" +
                        "      \"workspaceId\": 75887156211589,\n" +
                        "      \"workspaceName\": \"workspace1\"\n" +
                        "    }", OrgAndWorkspaceDto.class),
                parseJson("{\n" +
                        "      \"orgId\": 37736513644467,\n" +
                        "      \"orgName\": \"organization2\",\n" +
                        "      \"orgLogoUrl\": null,\n" +
                        "      \"workspaceId\": 75887156211590,\n" +
                        "      \"workspaceName\": \"workspace2\"\n" +
                        "    }", OrgAndWorkspaceDto.class)
        ), url(mock)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListByOrganization(OutputType format, MockServerClient mock) throws JsonProcessingException {
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

        ExecOut out = exec(format, mock, "workspaces", "list", "-o", "organization1");


        assertOutput(format, out, new WorkspaceList("jordi", List.of(parseJson(" {\n" +
                "      \"orgId\": 27736513644467,\n" +
                "      \"orgName\": \"organization1\",\n" +
                "      \"orgLogoUrl\": null,\n" +
                "      \"workspaceId\": 75887156211589,\n" +
                "      \"workspaceName\": \"workspace1\"\n" +
                "    }", OrgAndWorkspaceDto.class)
        ), url(mock)));
    }

    @Test
    void testListEmpty(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
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
        assertEquals(chop(new WorkspaceList("jordi", List.of(), url(mock)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteById(OutputType format, MockServerClient mock) {
        mock.reset();

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

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "workspaces", "delete", "-i", "75887156211589");
        assertOutput(format, out, new WorkspaceDeleted("workspace1", "organization1"));
    }

    @Test
    void testDeleteNotFound(MockServerClient mock) {
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

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "workspaces", "delete", "-i", "7588715621158");

        assertEquals(errorMessage(out.app, new WorkspaceNotFoundException(7588715621158L)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testViewById(OutputType format, MockServerClient mock) throws JsonProcessingException {
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

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "workspaces", "view", "-i", "75887156211589");

        assertOutput(format, out, new WorkspaceView(parseJson("{\n" +
                "    \"id\": 75887156211589,\n" +
                "    \"name\": \"workspace1\",\n" +
                "    \"fullName\": \"workspace 1\",\n" +
                "    \"description\": \"Workspace description\",\n" +
                "    \"visibility\": \"PRIVATE\",\n" +
                "    \"dateCreated\": \"2021-09-21T12:54:03Z\",\n" +
                "    \"lastUpdated\": \"2021-09-21T12:54:03Z\"\n" +
                "  }", Workspace.class)));
    }

    @Test
    void testViewNotFound(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"orgsAndWorkspaces\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "workspaces", "view", "-i", "7588715621158");

        assertEquals(errorMessage(out.app, new WorkspaceNotFoundException(7588715621158L)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
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

        ExecOut out = exec(format, mock, "workspaces", "add", "-n", "wspNew", "-o", "organization1", "-f", "wsp-new", "-d", "workspace description", "-v", "PRIVATE");
        assertOutput(format, out, new WorkspaceAdded("wspNew", "organization1", Visibility.PRIVATE));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddWithOverwrite(OutputType format, MockServerClient mock) {

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

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/validate").withQueryStringParameter("name", "workspace1"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        mock.when(
                request().withMethod("POST").withPath("/orgs/27736513644467/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(JsonBody.json("{\n" +
                        "  \"workspace\": {\n" +
                        "    \"id\": 52957572657821,\n" +
                        "    \"name\": \"workspace1\",\n" +
                        "    \"fullName\": \"wsp-new\",\n" +
                        "    \"description\": \"workspace description\",\n" +
                        "    \"visibility\": \"PRIVATE\",\n" +
                        "    \"dateCreated\": \"2021-09-28T07:15:26.696627Z\",\n" +
                        "    \"lastUpdated\": \"2021-09-28T07:15:26.696627Z\"\n" +
                        "  }\n" +
                        "}"))
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "workspaces", "add", "--overwrite", "-n", "workspace1", "-o", "organization1", "-f", "wsp-one", "-d", "workspace description", "-v", "PRIVATE");
        assertOutput(format, out, new WorkspaceAdded("workspace1", "organization1", Visibility.PRIVATE));
    }

    @Test
    void testAddOrganizationNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
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

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateById(OutputType format, MockServerClient mock) {
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

        ExecOut out = exec(format, mock, "workspaces", "update", "-i", "75887156211589", "-f", "wsp-new", "-d", "workspace description");
        assertOutput(format, out, new WorkspaceUpdated("workspace1", "organization1", Visibility.PRIVATE));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateNameById(OutputType format, MockServerClient mock) {

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

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_view")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589")
                        .withBody(json("{\"name\":\"workspace2\",\"fullName\":\"wsp-new\",\"description\":\"workspace description\",\"visibility\":\"PRIVATE\"}")),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_update_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "workspaces", "update", "-i", "75887156211589",
                "--new-name", "workspace2",
                "-f", "wsp-new",
                "-d", "workspace description"
        );
        assertOutput(format, out, new WorkspaceUpdated("workspace1", "organization1", Visibility.PRIVATE));
    }

    @Test
    void testUpdateWorkspaceNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"orgsAndWorkspaces\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "workspaces", "update", "-i", "7588715621158", "-f", "wsp-new", "-d", "workspace description");

        assertEquals(errorMessage(out.app, new WorkspaceNotFoundException(7588715621158L)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testLeaveWorkspaceAsParticipantById(OutputType format, MockServerClient mock) {
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

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589/participants"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "workspaces", "leave", "-i", "75887156211589");
        assertOutput(format, out, new ParticipantLeft("workspace1"));
    }

    @Test
    void leaveWorkspaceAsParticipantByIdAndWorkspaceReference(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(2)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589/participants"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "workspaces", "leave", "-n", "organization1/workspace1");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantLeft("workspace1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

}
