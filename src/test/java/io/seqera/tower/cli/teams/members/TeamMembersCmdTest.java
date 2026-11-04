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

package io.seqera.tower.cli.teams.members;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.teams.members.TeamMemberDeleted;
import io.seqera.tower.cli.responses.teams.members.TeamMembersAdd;
import io.seqera.tower.cli.responses.teams.members.TeamMembersList;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static io.seqera.tower.cli.utils.JsonHelper.prettyJson;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class TeamMembersCmdTest extends BaseCmdTest {

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

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams/267477500890054/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "teams", "members", "-o", "organization1", "-t", "team1");
        assertOutput(format, out, new TeamMembersList("team1", Arrays.asList(
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
        )));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws JsonProcessingException {
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

        mock.when(
                request().withMethod("POST").withPath("/orgs/27736513644467/teams/267477500890054/members")
                        .withBody("{\"userNameOrEmail\":\"abc@seqera.io\"}").withContentType(MediaType.APPLICATION_JSON), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("teams/members/member_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "teams", "members", "-o", "organization1", "-t", "team1", "add", "-m", "abc@seqera.io");
        assertOutput(format, out, new TeamMembersAdd("team1", parseJson("{\n" +
                "    \"memberId\": 42005399330152,\n" +
                "    \"userName\": \"abc\",\n" +
                "    \"email\": \"abc@seqera.io\",\n" +
                "    \"firstName\": null,\n" +
                "    \"lastName\": null,\n" +
                "    \"avatar\": null,\n" +
                "    \"role\": \"member\"\n" +
                "  }", MemberDbDto.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {
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

        ExecOut out = exec(format, mock, "teams", "members", "-o", "organization1", "-t", "team1", "delete", "-m", "julio2");
        assertOutput(format, out, new TeamMemberDeleted("team1", "julio2"));
    }

    @Test
    void testListPagesThroughEveryMember(MockServerClient mock) throws JsonProcessingException {
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

        List<MemberDbDto> members = LongStream.range(0, 101)
                .mapToObj(i -> new MemberDbDto().memberId(i).userName("user" + i).email("user" + i + "@seqera.io").role(OrgRole.member))
                .toList();

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams/267477500890054/members")
                        .withQueryStringParameter("max", "100")
                        .withQueryStringParameter("offset", "0"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(prettyJson(new ListMembersResponse().members(members.subList(0, 100)).totalSize(101L))).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/teams/267477500890054/members")
                        .withQueryStringParameter("max", "100")
                        .withQueryStringParameter("offset", "100"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(prettyJson(new ListMembersResponse().members(members.subList(100, 101)).totalSize(101L))).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(OutputType.json, mock, "teams", "members", "-o", "organization1", "-t", "team1");
        assertOutput(OutputType.json, out, new TeamMembersList("team1", members));
    }
}
