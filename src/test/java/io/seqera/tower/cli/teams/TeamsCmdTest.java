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

package io.seqera.tower.cli.teams;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.teams.TeamAdded;
import io.seqera.tower.cli.responses.teams.TeamDeleted;
import io.seqera.tower.cli.responses.teams.TeamsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.TeamDbDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

class TeamsCmdTest extends BaseCmdTest {

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

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "teams", "list", "-o", "organization1");
        assertOutput(format, out, new TeamsList("organization1", Arrays.asList(
                parseJson(" {\n" +
                        "      \"teamId\": 249211453903161,\n" +
                        "      \"name\": \"team-test-3\",\n" +
                        "      \"description\": \"AAAAAA\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson(" {\n" +
                        "      \"teamId\": 69076469523589,\n" +
                        "      \"name\": \"team-test-1\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson(" {\n" +
                        "      \"teamId\": 255717345477198,\n" +
                        "      \"name\": \"team-test-2\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson("{\n" +
                        "      \"teamId\": 267477500890054,\n" +
                        "      \"name\": \"team1\",\n" +
                        "      \"description\": \"Team 1\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 1\n" +
                        "    }", TeamDbDto.class)
        ), baseOrgUrl(mock, "organization1"), null));
    }

    @Test
    void testListWithOffset(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/teams")
                        .withQueryStringParameter("offset", "1")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list_redux")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "list", "-o", "organization1", "--offset", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new TeamsList("organization1", Arrays.asList(
                parseJson(" {\n" +
                        "      \"teamId\": 69076469523589,\n" +
                        "      \"name\": \"team-test-1\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson(" {\n" +
                        "      \"teamId\": 255717345477198,\n" +
                        "      \"name\": \"team-test-2\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class)
        ), baseOrgUrl(mock, "organization1"), PaginationInfo.from(1, 2)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListWithPage(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/teams")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "list", "-o", "organization1", "--page", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new TeamsList("organization1", Arrays.asList(
                parseJson(" {\n" +
                        "      \"teamId\": 249211453903161,\n" +
                        "      \"name\": \"team-test-3\",\n" +
                        "      \"description\": \"AAAAAA\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson(" {\n" +
                        "      \"teamId\": 69076469523589,\n" +
                        "      \"name\": \"team-test-1\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson(" {\n" +
                        "      \"teamId\": 255717345477198,\n" +
                        "      \"name\": \"team-test-2\",\n" +
                        "      \"description\": \"a new team\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 0\n" +
                        "    }", TeamDbDto.class),
                parseJson("{\n" +
                        "      \"teamId\": 267477500890054,\n" +
                        "      \"name\": \"team1\",\n" +
                        "      \"description\": \"Team 1\",\n" +
                        "      \"avatarUrl\": null,\n" +
                        "      \"membersCount\": 1\n" +
                        "    }", TeamDbDto.class)
        ), baseOrgUrl(mock, "organization1"), PaginationInfo.from(null, 2, 1, null)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListWithConflictingPageable(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/teams")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "list", "-o", "organization1", "--page", "1", "--offset", "0", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --page or --offset as pagination parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
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
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"teams\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "list", "-o", "organization1");

        assertEquals("", out.stdErr);
        assertEquals(chop(new TeamsList("organization1", List.of(), baseOrgUrl(mock, "organization1"), null).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
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
                request().withMethod("POST").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "teams", "add", "-o", "organization1", "-n", "team-test");
        assertOutput(format, out, new TeamAdded("organization1", "team-test"));
    }

    @Test
    void testAddWithOrganizationNotFound(MockServerClient mock) {
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

        ExecOut out = exec(mock, "teams", "add", "-o", "organization-not-found", "-n", "team-test");

        assertEquals(errorMessage(out.app, new OrganizationNotFoundException("organization-not-found")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteTeam(OutputType format, MockServerClient mock) {
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
                request().withMethod("DELETE").withPath("/orgs/27736513644467/teams/69076469523589"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "teams", "delete", "-o", "organization1", "-i", "69076469523589");
        assertOutput(format, out, new TeamDeleted("organization1", "69076469523589"));
    }
}
