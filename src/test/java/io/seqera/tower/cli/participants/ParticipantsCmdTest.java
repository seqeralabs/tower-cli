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

package io.seqera.tower.cli.participants;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.participants.ParticipantAdded;
import io.seqera.tower.cli.responses.participants.ParticipantChanged;
import io.seqera.tower.cli.responses.participants.ParticipantDeleted;
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import io.seqera.tower.cli.responses.participants.ParticipantsList;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.WspRole;
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

public class ParticipantsCmdTest extends BaseCmdTest {

    @Test
    void testListAllParticipants(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "list", "-w", "75887156211589");

        assertEquals("", out.stdErr);
        assertEquals(chop(new ParticipantsList("organization1", "workspace1",
                Arrays.asList(
                        parseJson("{\n" +
                                "      \"participantId\": 48516118433516,\n" +
                                "      \"memberId\": 175703974560466,\n" +
                                "      \"userName\": \"jfernandez74\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"jfernandez74@gmail.com\",\n" +
                                "      \"orgRole\": \"owner\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"owner\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\"\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson(" {\n" +
                                "      \"participantId\": 36791779798370,\n" +
                                "      \"memberId\": 255080245994226,\n" +
                                "      \"userName\": \"julio\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"julio@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"admin\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\"\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 110330443206779,\n" +
                                "      \"memberId\": 80726606082762,\n" +
                                "      \"userName\": \"julio2\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"julio2@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 110330443206780,\n" +
                                "      \"memberId\": 80726606082770,\n" +
                                "      \"userName\": \"jordi\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"jordi@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 179548688376545,\n" +
                                "      \"memberId\": null,\n" +
                                "      \"userName\": null,\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": null,\n" +
                                "      \"orgRole\": null,\n" +
                                "      \"teamId\": 255717345477198,\n" +
                                "      \"teamName\": \"team-test-2\",\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"TEAM\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class))).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListAllParticipantsWithOffset(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants")
                        .withQueryStringParameter("offset", "1")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "list", "-w", "75887156211589", "--offset", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new ParticipantsList("organization1", "workspace1",
                Arrays.asList(
                        parseJson("{\n" +
                                "      \"participantId\": 48516118433516,\n" +
                                "      \"memberId\": 175703974560466,\n" +
                                "      \"userName\": \"jfernandez74\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"jfernandez74@gmail.com\",\n" +
                                "      \"orgRole\": \"owner\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"owner\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\"\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson(" {\n" +
                                "      \"participantId\": 36791779798370,\n" +
                                "      \"memberId\": 255080245994226,\n" +
                                "      \"userName\": \"julio\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"julio@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"admin\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\"\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 110330443206779,\n" +
                                "      \"memberId\": 80726606082762,\n" +
                                "      \"userName\": \"julio2\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"julio2@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 110330443206780,\n" +
                                "      \"memberId\": 80726606082770,\n" +
                                "      \"userName\": \"jordi\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"jordi@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 179548688376545,\n" +
                                "      \"memberId\": null,\n" +
                                "      \"userName\": null,\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": null,\n" +
                                "      \"orgRole\": null,\n" +
                                "      \"teamId\": 255717345477198,\n" +
                                "      \"teamName\": \"team-test-2\",\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"TEAM\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class))).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListAllParticipantsWithPage(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "list", "-w", "75887156211589", "--page", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(chop(new ParticipantsList("organization1", "workspace1",
                Arrays.asList(
                        parseJson("{\n" +
                                "      \"participantId\": 48516118433516,\n" +
                                "      \"memberId\": 175703974560466,\n" +
                                "      \"userName\": \"jfernandez74\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"jfernandez74@gmail.com\",\n" +
                                "      \"orgRole\": \"owner\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"owner\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\"\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson(" {\n" +
                                "      \"participantId\": 36791779798370,\n" +
                                "      \"memberId\": 255080245994226,\n" +
                                "      \"userName\": \"julio\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"julio@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"admin\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\"\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 110330443206779,\n" +
                                "      \"memberId\": 80726606082762,\n" +
                                "      \"userName\": \"julio2\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"julio2@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 110330443206780,\n" +
                                "      \"memberId\": 80726606082770,\n" +
                                "      \"userName\": \"jordi\",\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": \"jordi@seqera.io\",\n" +
                                "      \"orgRole\": \"member\",\n" +
                                "      \"teamId\": null,\n" +
                                "      \"teamName\": null,\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"MEMBER\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class),
                        parseJson("{\n" +
                                "      \"participantId\": 179548688376545,\n" +
                                "      \"memberId\": null,\n" +
                                "      \"userName\": null,\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": null,\n" +
                                "      \"orgRole\": null,\n" +
                                "      \"teamId\": 255717345477198,\n" +
                                "      \"teamName\": \"team-test-2\",\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"TEAM\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class))).toString()), out.stdOut);
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "list", "-w", "75887156211589", "--page", "1", "--offset", "0", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --page or --offset as pagination parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testListWithConflictingSizeable(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "list", "-w", "75887156211589", "--page", "1", "--no-max", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --no-max or --max as pagination size parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(-1, out.exitCode);
    }

    @Test
    void testListTeamsParticipants(MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "list", "-w", "75887156211589", "-t", "TEAM");

        assertEquals("", out.stdErr);
        assertEquals(chop(new ParticipantsList("organization1", "workspace1",
                List.of(
                        parseJson("{\n" +
                                "      \"participantId\": 179548688376545,\n" +
                                "      \"memberId\": null,\n" +
                                "      \"userName\": null,\n" +
                                "      \"firstName\": null,\n" +
                                "      \"lastName\": null,\n" +
                                "      \"email\": null,\n" +
                                "      \"orgRole\": null,\n" +
                                "      \"teamId\": 255717345477198,\n" +
                                "      \"teamName\": \"team-test-2\",\n" +
                                "      \"wspRole\": \"launch\",\n" +
                                "      \"type\": \"TEAM\",\n" +
                                "      \"teamAvatarUrl\": null,\n" +
                                "      \"userAvatarUrl\": null\n" +
                                "    }", ParticipantDbDto.class))).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testDeleteMemberParticipant(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants").withQueryStringParameter("search", "julio"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/36791779798370"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "participants", "delete", "-w", "75887156211589", "-n", "julio", "-t", "MEMBER");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantDeleted("julio", "workspace1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testDeleteTeamParticipant(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants").withQueryStringParameter("search", "julio"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/179548688376545"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "participants", "delete", "-w", "75887156211589", "-n", "julio", "-t", "TEAM");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantDeleted("julio", "workspace1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testLeaveAsParticipant(MockServerClient mock) {
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

        ExecOut out = exec(mock, "participants", "leave", "-w", "75887156211589");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantLeft("workspace1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testChangeMemberParticipantRole(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants").withQueryStringParameter("search", "julio"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/36791779798370/role"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "participants", "change", "-w", "75887156211589", "-n", "julio", "-r", "OWNER", "-t", "MEMBER");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantChanged("workspace1", "julio", WspRole.OWNER.toString()).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testChangeTeamParticipantRole(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants").withQueryStringParameter("search", "julio"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/179548688376545/role"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "participants", "change", "-w", "75887156211589", "-n", "julio", "-r", "OWNER", "-t", "TEAM");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantChanged("workspace1", "julio", WspRole.OWNER.toString()).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testCreateMemberParticipantRole(MockServerClient mock) throws JsonProcessingException {
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
                response().withStatusCode(200).withBody(loadResource("members/members_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/add"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participant_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "add", "-w", "75887156211589", "-n", "julio", "-t", "MEMBER");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantAdded(parseJson("{\n" +
                "    \"participantId\": 110330443206779,\n" +
                "    \"memberId\": 80726606082762,\n" +
                "    \"userName\": \"julio\",\n" +
                "    \"firstName\": null,\n" +
                "    \"lastName\": null,\n" +
                "    \"email\": \"user@seqera.io\",\n" +
                "    \"orgRole\": \"member\",\n" +
                "    \"teamId\": null,\n" +
                "    \"teamName\": null,\n" +
                "    \"wspRole\": \"launch\",\n" +
                "    \"type\": \"MEMBER\",\n" +
                "    \"teamAvatarUrl\": null,\n" +
                "    \"userAvatarUrl\": null\n" +
                "  }", ParticipantDbDto.class), "workspace1").toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
