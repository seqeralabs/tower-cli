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

package io.seqera.tower.cli.teams.members;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.responses.teams.members.TeamMemberDeleted;
import io.seqera.tower.cli.responses.teams.members.TeamMembersAdd;
import io.seqera.tower.cli.responses.teams.members.TeamMembersList;
import io.seqera.tower.model.MemberDbDto;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Arrays;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TeamMembersCmdTest extends BaseCmdTest {

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

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams/267477500890054/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "members", "-o", "organization1", "-t", "team1");

        assertEquals("", out.stdErr);
        assertEquals(chop(new TeamMembersList("team1", Arrays.asList(
                parseJson("   {\n" +
                        "      \"memberId\": 80726606082762,\n" +
                        "      \"userName\": \"julio2\",\n" +
                        "      \"email\": \"julio2@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": null,\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class),
                parseJson(" {\n" +
                        "      \"memberId\": 142050384398323,\n" +
                        "      \"userName\": \"julio789\",\n" +
                        "      \"email\": \"julio789@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": null,\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class),
                parseJson(" {\n" +
                        "      \"memberId\": 199966343791197,\n" +
                        "      \"userName\": \"julio7899\",\n" +
                        "      \"email\": \"julio7899@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": null,\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testAdd(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/orgs/27736513644467/teams/267477500890054/members")
                        .withBody("{\"userNameOrEmail\":\"abc@seqera.io\"}").withContentType(MediaType.APPLICATION_JSON), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/members/member_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "teams", "members", "-o", "organization1", "-t", "team1", "add", "-m", "abc@seqera.io");

        assertEquals("", out.stdErr);
        assertEquals(new TeamMembersAdd("team1", parseJson("{\n" +
                "    \"memberId\": 42005399330152,\n" +
                "    \"userName\": \"abc\",\n" +
                "    \"email\": \"abc@seqera.io\",\n" +
                "    \"firstName\": null,\n" +
                "    \"lastName\": null,\n" +
                "    \"avatar\": null,\n" +
                "    \"role\": \"member\"\n" +
                "  }", MemberDbDto.class)).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testDelete(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/teams"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/teams_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams/267477500890054/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/teams/267477500890054/members/80726606082762/delete"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "teams", "members", "-o", "organization1", "-t", "team1", "delete", "-m", "julio2");

        assertEquals("", out.stdErr);
        assertEquals(new TeamMemberDeleted("team1", "julio2").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
