/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.collaborators;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.collaborators.CollaboratorsList;
import io.seqera.tower.model.MemberDbDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Arrays;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class CollaboratorsCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListCollaborators(OutputType format, MockServerClient mock) throws JsonProcessingException {
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
                request().withMethod("GET").withPath("/orgs/27736513644467/collaborators")
                        .withQueryStringParameter("max", "100")
                        .withQueryStringParameter("offset", "0"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("collaborators/collaborators_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "collaborators", "list", "-o", "27736513644467");
        assertOutput(format, out, new CollaboratorsList(27736513644467L, Arrays.asList(
                parseJson(" {\n" +
                        "      \"memberId\": 175703974560466,\n" +
                        "      \"userName\": \"jfernandez74\",\n" +
                        "      \"email\": \"jfernandez74@gmail.com\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\",\n" +
                        "      \"role\" : \"collaborator\"" +
                        "    }", MemberDbDto.class),
                parseJson("{\n" +
                        "      \"memberId\": 255080245994226,\n" +
                        "      \"userName\": \"julio\",\n" +
                        "      \"email\": \"julio@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\",\n" +
                        "      \"role\" : \"collaborator\"" +
                        "    }", MemberDbDto.class)
        ), null));
    }
}
