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

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.seqera.tower.cli.credentials;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.CredentialsDeleted;
import io.seqera.tower.cli.responses.CredentialsList;
import io.seqera.tower.model.Credentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.Arrays;
import java.util.List;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class CredentialsCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/credentials/1cz5A8cuBkB5iJliCwJCFU"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "credentials", "delete", "-i", "1cz5A8cuBkB5iJliCwJCFU");
        assertOutput(format, out, new CredentialsDeleted("1cz5A8cuBkB5iJliCwJCFU", USER_WORKSPACE_NAME));
    }

    @Test
    void testDeleteNotFound(MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/credentials/1cz5A8cuBkB5iKKiCwJCFU"), exactly(1)
        ).respond(
                response().withStatusCode(403)
        );

        ExecOut out = exec(mock, "credentials", "delete", "-i", "1cz5A8cuBkB5iKKiCwJCFU");

        assertEquals(errorMessage(out.app, new CredentialsNotFoundException("1cz5A8cuBkB5iKKiCwJCFU", USER_WORKSPACE_NAME)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/credentials"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("credentials_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "credentials", "list");
        assertOutput(format, out, new CredentialsList(USER_WORKSPACE_NAME, Arrays.asList(
                parseJson("{\"id\": \"2ba2oekqeTEBzwSDgXg7xf\", \"lastUsed\": \"2021-09-06T08:53:51Z\", \"dateCreated\":\"2021-09-06T06:54:53Z\", \"lastUpdated\":\"2021-09-06T06:54:53Z\"}", Credentials.class)
                        .name("ssh")
                        .provider(Credentials.ProviderEnum.SSH),
                parseJson("{\"id\": \"57Ic6reczFn78H1DTaaXkp\", \"dateCreated\":\"2021-09-07T13:50:21Z\", \"lastUpdated\":\"2021-09-07T13:50:21Z\"}", Credentials.class)
                        .name("azure")
                        .provider(Credentials.ProviderEnum.AZURE)
        ), baseUserUrl(mock, USER_WORKSPACE_NAME)));
    }

    @Test
    void testListEmpty(MockServerClient mock) {

        mock.when(
                request().withMethod("GET").withPath("/credentials"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"credentials\": []}").withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/user-info"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "credentials", "list");

        assertEquals("", out.stdErr);
        assertEquals(chop(new CredentialsList(USER_WORKSPACE_NAME, List.of(), baseUserUrl(mock, USER_WORKSPACE_NAME)).toString()), out.stdOut);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testInvalidAuth(MockServerClient mock) {
        mock.when(
                request().withMethod("DELETE").withPath("/credentials/1cz5A8cuBkB5iJliCwJCFT"), exactly(1)
        ).respond(
                response().withStatusCode(401)
        );

        ExecOut out = exec(mock, "credentials", "delete", "-i", "1cz5A8cuBkB5iJliCwJCFT");

        assertEquals(errorMessage(out.app, new ApiException(401, "Unauthorized")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testShowUsage(MockServerClient mock) {

        ExecOut out = exec(mock, "credentials");

        if (out.app != null) {
            assertEquals(errorMessage(out.app, new ShowUsageException(out.app.spec.subcommands().get("credentials").getCommandSpec())), out.stdErr);
            assertEquals("", out.stdOut);
            assertEquals(1, out.exitCode);
        }
    }

    @Test
    void testMissingSubcommand(MockServerClient mock) {

        ExecOut out = exec(mock, "credentials","add");

        if (out.app != null) {
            assertEquals("", out.stdOut);
            assertEquals(1, out.exitCode);
            assertTrue(out.stdErr.contains("Missing Required Subcommand"));
        }
    }


}
