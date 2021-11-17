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

package io.seqera.tower.cli.collaborators;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.responses.collaborators.CollaboratorsList;
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

public class CollaboratorsCmdTest extends BaseCmdTest {

    @Test
    void testListCollaborators(MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/orgs/27736513644467/collaborators")
                        .withQueryStringParameter("max", "100")
                        .withQueryStringParameter("offset", "0"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("collaborators/collaborators_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "collaborators", "list", "-o", "27736513644467");

        assertEquals("", out.stdErr);
        assertEquals(chop(new CollaboratorsList(27736513644467L, Arrays.asList(
                parseJson(" {\n" +
                        "      \"memberId\": 175703974560466,\n" +
                        "      \"userName\": \"jfernandez74\",\n" +
                        "      \"email\": \"jfernandez74@gmail.com\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/7d3c1ee212a3465233e161b451fb4d05?d=404\"\n" +
                        "    }", MemberDbDto.class),
                parseJson("{\n" +
                        "      \"memberId\": 255080245994226,\n" +
                        "      \"userName\": \"julio\",\n" +
                        "      \"email\": \"julio@seqera.io\",\n" +
                        "      \"firstName\": null,\n" +
                        "      \"lastName\": null,\n" +
                        "      \"avatar\": \"https://www.gravatar.com/avatar/72918a9f674eaa696729917bec58760b?d=404\"\n" +
                        "    }", MemberDbDto.class)
        )).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }
}
