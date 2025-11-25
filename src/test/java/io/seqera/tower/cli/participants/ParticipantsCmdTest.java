/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.participants;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.participants.ParticipantAdded;
import io.seqera.tower.cli.responses.participants.ParticipantDeleted;
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import io.seqera.tower.cli.responses.participants.ParticipantUpdated;
import io.seqera.tower.cli.responses.participants.ParticipantsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ParticipantResponseDto;
import io.seqera.tower.cli.commands.enums.WspRole;
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

class ParticipantsCmdTest extends BaseCmdTest {

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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "participants", "list", "-w", "75887156211589");

        assertOutput(format, out, new ParticipantsList("organization1", "workspace1",
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class)), null));
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class)), PaginationInfo.from(1, 2, null, null)).toString()), out.stdOut);
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class),
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
                                "    }", ParticipantResponseDto.class)), PaginationInfo.from(null, 2, 1, null)).toString()), out.stdOut);
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants")
                        .withQueryStringParameter("offset", "0")
                        .withQueryStringParameter("max", "2"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "participants", "list", "-w", "75887156211589", "--page", "1", "--offset", "0", "--max", "2");

        assertEquals(errorMessage(out.app, new TowerException("Please use either --page or --offset as pagination parameter")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testListTeam(MockServerClient mock) throws JsonProcessingException {
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
                                "    }", ParticipantResponseDto.class)), null).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteMemberParticipant(OutputType format, MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants").withQueryStringParameter("search", "julio"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/36791779798370"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "participants", "delete", "-w", "75887156211589", "-n", "julio", "-t", "MEMBER");
        assertOutput(format, out, new ParticipantDeleted("julio", "workspace1"));
    }

    @Test
    void testDeleteTeam(MockServerClient mock) {
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

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testLeave(OutputType format, MockServerClient mock) {
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

        ExecOut out = exec(format, mock, "participants", "leave", "-w", "75887156211589");
        assertOutput(format, out, new ParticipantLeft("workspace1"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateMemberParticipantRole(OutputType format, MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants").withQueryStringParameter("search", "julio"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/36791779798370/role"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "participants", "update", "-w", "75887156211589", "-n", "julio", "-r", "OWNER", "-t", "MEMBER");
        assertOutput(format, out, new ParticipantUpdated("workspace1", "julio", WspRole.owner.toString()));
    }

    @Test
    void testUpdateTeamParticipantRole(MockServerClient mock) {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants").withQueryStringParameter("search", "julio"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/179548688376545/role"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(mock, "participants", "update", "-w", "75887156211589", "-n", "julio", "-r", "OWNER", "-t", "TEAM");

        assertEquals("", out.stdErr);
        assertEquals(new ParticipantUpdated("workspace1", "julio", WspRole.owner.toString()).toString(), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddMemberParticipantRole(OutputType format, MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/members"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("members/members_list_filter")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/add"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participant_add")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "participants", "add", "-w", "75887156211589", "-n", "julio", "-t", "MEMBER");

        assertOutput(format, out, new ParticipantAdded(parseJson("{\n" +
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
                "  }", ParticipantResponseDto.class), "workspace1"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddParticipantWithOverwrite(OutputType format, MockServerClient mock) throws JsonProcessingException {

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
                request().withMethod("GET").withPath("/orgs/27736513644467/workspaces/75887156211589/participants"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("participants/participants_list")).withContentType(MediaType.APPLICATION_JSON)
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

        mock.when(
                request().withMethod("DELETE").withPath("/orgs/27736513644467/workspaces/75887156211589/participants/48516118433516"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "participants", "add", "--overwrite", "-w", "75887156211589", "-n", "julio", "-t", "MEMBER");

        assertOutput(format, out, new ParticipantAdded(parseJson("{\n" +
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
                "  }", ParticipantResponseDto.class), "workspace1"));
    }
}
