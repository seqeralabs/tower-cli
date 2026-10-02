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


package io.seqera.tower.cli.credentials;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.CredentialsFederationSetup;
import io.seqera.tower.model.CredentialsSetupValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.List;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class FederationSetupCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testFederationSetup(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/credentials/federation-setup").withQueryStringParameter("provider", "aws"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"setupValues\":[" +
                        "{\"label\":\"Platform public address\",\"value\":\"https://cloud.seqera.io\"}," +
                        "{\"label\":\"Audience\",\"value\":\"sts.amazonaws.com\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "credentials", "federation-setup", "-p", "aws");

        assertOutput(format, out, new CredentialsFederationSetup("aws", USER_WORKSPACE_NAME, List.of(
                new CredentialsSetupValue().label("Platform public address").value("https://cloud.seqera.io"),
                new CredentialsSetupValue().label("Audience").value("sts.amazonaws.com")
        )));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testFederationSetupWithoutValues(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/credentials/federation-setup").withQueryStringParameter("provider", "azure"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"setupValues\":[]}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "credentials", "federation-setup", "-p", "azure");

        assertOutput(format, out, new CredentialsFederationSetup("azure", USER_WORKSPACE_NAME, List.of()));
    }
}
