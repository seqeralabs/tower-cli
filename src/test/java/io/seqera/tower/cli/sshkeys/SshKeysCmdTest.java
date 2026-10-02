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


package io.seqera.tower.cli.sshkeys;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.sshkeys.SshKeyAdded;
import io.seqera.tower.cli.responses.sshkeys.SshKeyDeleted;
import io.seqera.tower.cli.responses.sshkeys.SshKeyView;
import io.seqera.tower.cli.responses.sshkeys.SshKeysList;
import io.seqera.tower.model.UserSshPublicKeyDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class SshKeysCmdTest extends BaseCmdTest {

    private static final String KEY_1 = "{\"id\":11,\"name\":\"laptop\",\"publicKey\":\"ssh-ed25519 AAAAC3Nza laptop\",\"dateCreated\":\"2026-09-01T10:00:00Z\",\"lastUsed\":null}";
    private static final String KEY_2 = "{\"id\":12,\"name\":\"desktop\",\"publicKey\":\"ssh-ed25519 AAAAC3Nzb desktop\",\"dateCreated\":\"2026-09-02T10:00:00Z\",\"lastUsed\":\"2026-09-03T10:00:00Z\"}";
    private static final String LIST = "{\"sshKeys\":[" + KEY_1 + "," + KEY_2 + "]}";

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/ssh-keys"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(LIST).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "ssh-keys", "list");

        assertOutput(format, out, new SshKeysList(List.of(
                parseJson(KEY_1, UserSshPublicKeyDto.class),
                parseJson(KEY_2, UserSshPublicKeyDto.class)
        )));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("POST").withPath("/ssh-keys")
                        .withBody(json("{\"name\":\"laptop\",\"publicKey\":\"ssh-ed25519 AAAAC3Nza laptop\"}")),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"sshKey\":" + KEY_1 + "}").withContentType(MediaType.APPLICATION_JSON)
        );

        // The trailing newline of the .pub file is not sent
        ExecOut out = exec(format, mock, "ssh-keys", "add", "-n", "laptop", "-k", tempFile("ssh-ed25519 AAAAC3Nza laptop\n", "id_ed25519", ".pub"));

        assertOutput(format, out, new SshKeyAdded(11L, "laptop"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testViewById(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/ssh-keys/12"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"sshKey\":" + KEY_2 + "}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "ssh-keys", "view", "-i", "12");

        assertOutput(format, out, new SshKeyView(parseJson(KEY_2, UserSshPublicKeyDto.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteByName(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/ssh-keys"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(LIST).withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("DELETE").withPath("/ssh-keys/12"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "ssh-keys", "delete", "-n", "desktop");

        assertOutput(format, out, new SshKeyDeleted(12L, "desktop"));
    }

    @Test
    void testDeleteUnknownName(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/ssh-keys"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(LIST).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "ssh-keys", "delete", "-n", "missing");

        assertEquals(1, out.exitCode);
        assertTrue(out.stdErr.contains("Unknown SSH key 'missing'"), out.stdErr);
    }
}
