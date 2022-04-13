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

package io.seqera.tower.cli.members;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.members.MembersAdded;
import io.seqera.tower.cli.responses.members.MembersDeleted;
import io.seqera.tower.cli.responses.members.MembersLeave;
import io.seqera.tower.cli.responses.members.MembersList;
import io.seqera.tower.cli.responses.members.MembersUpdate;
import io.seqera.tower.model.MemberDbDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Arrays;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class MembersCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "members", "list", "-o", "organization1");
        assertOutput(format, out, new MembersList("organization1", Arrays.asList(
                parseJson(" {\n" +
                        "      \"memberId\": 175703974560466,\n" +
                        "      \"userName\": \"jfernandez74\",\n" +
                        "      \"email\": \"jfernandez74@gmail.com\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\",\n" +
                        "      \"role\": \"owner\"\n" +
                        "    }", MemberDbDto.class),
                parseJson("{\n" +
                        "      \"memberId\": 255080245994226,\n" +
                        "      \"userName\": \"julio\",\n" +
                        "      \"email\": \"julio@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\",\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class)
        )));
    }

    @Test
    void testListWithOffset(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/members")
                        .withQueryStringParameter("offset", "1")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "members", "list", "-o", "organization1", "--offset", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new MembersList("organization1", Arrays.asList(
                parseJson(" {\n" +
                        "      \"memberId\": 175703974560466,\n" +
                        "      \"userName\": \"jfernandez74\",\n" +
                        "      \"email\": \"jfernandez74@gmail.com\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\",\n" +
                        "      \"role\": \"owner\"\n" +
                        "    }", MemberDbDto.class),
                parseJson("{\n" +
                        "      \"memberId\": 255080245994226,\n" +
                        "      \"userName\": \"julio\",\n" +
                        "      \"email\": \"julio@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\",\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListWithPage(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/members")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "members", "list", "-o", "organization1", "--page", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new MembersList("organization1", Arrays.asList(
                parseJson(" {\n" +
                        "      \"memberId\": 175703974560466,\n" +
                        "      \"userName\": \"jfernandez74\",\n" +
                        "      \"email\": \"jfernandez74@gmail.com\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\",\n" +
                        "      \"role\": \"owner\"\n" +
                        "    }", MemberDbDto.class),
                parseJson("{\n" +
                        "      \"memberId\": 255080245994226,\n" +
                        "      \"userName\": \"julio\",\n" +
                        "      \"email\": \"julio@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\",\n" +
                        "      \"role\": \"member\"\n" +
                        "    }", MemberDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListWithConflictingPageable(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/members")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/members_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "members", "list", "-o", "organization1", "--page", "1", "--offset", "0", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --page or --offset as pagination parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("PUT").withPath("/orgs/27736513644467/members/add"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/member_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "members", "add", "-o", "organization1", "-u", "julio123");

        assertOutput(format, out, new MembersAdded("organization1", parseJson("{\n" +
                "    \"memberId\": 440905637173,\n" +
                "    \"userName\": \"julio123\",\n" +
                "    \"email\": \"julio123@seqera.io\",\n" +
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
                request().withMethod("GET").withPath("/orgs/27736513644467/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/members_list_filtered")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/members/255080245994226"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "members", "delete", "-o", "organization1", "-u", "julio@seqera.io");
        assertOutput(format, out, new MembersDeleted("julio@seqera.io", "organization1"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdate(OutputType format, MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/members_list_filtered")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/members/255080245994226/role").withBody("{\"role\":\"owner\"}").withContentType(MediaType.APPLICATION_JSON), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "members", "update", "-o", "organization1", "-u", "julio@seqera.io", "-r", "OWNER");
        assertOutput(format, out, new MembersUpdate("julio@seqera.io", "organization1", "owner"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testLeave(OutputType format, MockServerClient mock) {
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
                request().withMethod("DELETE").withPath("/orgs/27736513644467/members/leave"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "members", "leave", "-o", "organization1");
        assertOutput(format, out, new MembersLeave("organization1"));
    }
}
