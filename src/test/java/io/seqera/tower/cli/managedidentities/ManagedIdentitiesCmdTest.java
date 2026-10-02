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


package io.seqera.tower.cli.managedidentities;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.managedidentities.ManagedCredentialsAdded;
import io.seqera.tower.cli.responses.managedidentities.ManagedCredentialsDeleted;
import io.seqera.tower.cli.responses.managedidentities.ManagedCredentialsList;
import io.seqera.tower.cli.responses.managedidentities.ManagedCredentialsUpdated;
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentitiesList;
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentityAdded;
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentityDeleted;
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentityUpdated;
import io.seqera.tower.cli.responses.managedidentities.ManagedIdentityView;
import io.seqera.tower.model.ListManagedCredentialsRespDto;
import io.seqera.tower.model.ManagedIdentityDbDtoAbstractGridConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.util.List;
import java.util.Collections;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class ManagedIdentitiesCmdTest extends BaseCmdTest {

    private static final String ORG_ID = "27736513644467";
    private static final String IDENTITY = "{\"id\":31,\"name\":\"hpc1\",\"platform\":\"slurm-platform\",\"config\":{\"discriminator\":\"slurm-platform\",\"hostName\":\"login.example.com\",\"port\":22}}";
    private static final String IDENTITIES = "{\"managedIdentities\":[" + IDENTITY + ",{\"id\":32,\"name\":\"hpc2\",\"platform\":\"lsf-platform\",\"config\":{\"discriminator\":\"lsf-platform\",\"hostName\":\"lsf.example.com\",\"port\":2222}}],\"totalSize\":2}";
    private static final String OWN_ROW = "{\"managedCredentialsId\":41,\"userId\":1264,\"userName\":\"jordi\",\"provider\":\"ssh\",\"metadata\":{\"discriminator\":\"ssh\",\"userName\":\"jordi_hpc\"}}";
    private static final String MISSING_ROW = "{\"managedCredentialsId\":null,\"userId\":77,\"userName\":\"alice\",\"firstName\":\"Alice\",\"lastName\":\"Doe\"}";
    private static final String CREDENTIALS = "{\"managedCredentials\":[" + OWN_ROW + "," + MISSING_ROW + "],\"totalSize\":2}";

    @BeforeEach
    void mockOrganization(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/user-info")
        ).respond(
                response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON)
        );
        mock.when(
                request().withMethod("GET").withPath("/user/1264/workspaces")
        ).respond(
                response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    private void mockIdentities(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/identities").withQueryStringParameter("orgId", ORG_ID), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(IDENTITIES).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    private void mockCredentials(MockServerClient mock, String search) {
        mock.when(
                search == null
                        ? request().withMethod("GET").withPath("/identities/31/credentials").withQueryStringParameter("orgId", ORG_ID)
                        : request().withMethod("GET").withPath("/identities/31/credentials").withQueryStringParameter("orgId", ORG_ID).withQueryStringParameter("search", search),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(CREDENTIALS).withContentType(MediaType.APPLICATION_JSON)
        );
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws IOException {
        mockIdentities(mock);

        ExecOut out = exec(format, mock, "managed-identities", "list", "-o", "organization1");

        assertOutput(format, out, new ManagedIdentitiesList("organization1",
                parseJson(IDENTITIES, io.seqera.tower.model.ListManagedIdentitiesResponse.class).getManagedIdentities()));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("POST").withPath("/identities").withQueryStringParameter("orgId", ORG_ID)
                        .withBody(json("{\"name\":\"hpc1\",\"platform\":\"slurm-platform\",\"config\":{\"discriminator\":\"slurm-platform\",\"hostName\":\"login.example.com\",\"port\":22}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(IDENTITY).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "managed-identities", "add", "-o", "organization1", "-n", "hpc1", "-p", "slurm", "-H", "login.example.com");

        assertOutput(format, out, new ManagedIdentityAdded("organization1", 31L, "hpc1"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testViewByName(OutputType format, MockServerClient mock) throws IOException {
        mockIdentities(mock);

        ExecOut out = exec(format, mock, "managed-identities", "view", "-o", "organization1", "-n", "hpc1");

        assertOutput(format, out, new ManagedIdentityView("organization1", parseJson(IDENTITY, ManagedIdentityDbDtoAbstractGridConfig.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdate(OutputType format, MockServerClient mock) {
        mockIdentities(mock);
        // Keeps the current name and port, sends the platform so Platform can read the config
        mock.when(
                request().withMethod("PUT").withPath("/identities/31").withQueryStringParameter("orgId", ORG_ID)
                        .withBody(json("{\"managedIdentity\":{\"name\":\"hpc1\",\"platform\":\"slurm-platform\",\"config\":{\"discriminator\":\"slurm-platform\",\"hostName\":\"new.example.com\",\"port\":22}}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "managed-identities", "update", "-o", "organization1", "-i", "31", "-H", "new.example.com");

        assertOutput(format, out, new ManagedIdentityUpdated("organization1", 31L, "hpc1"));
    }

    @Test
    void testUpdateWithoutChanges(MockServerClient mock) {
        ExecOut out = exec(mock, "managed-identities", "update", "-o", "organization1", "-i", "31");

        assertEquals(1, out.exitCode);
        assertTrue(out.stdErr.contains("Nothing to update"), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDeleteForce(OutputType format, MockServerClient mock) {
        mockIdentities(mock);
        mock.when(
                request().withMethod("DELETE").withPath("/identities/32").withQueryStringParameter("orgId", ORG_ID).withQueryStringParameter("checked", "false"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "managed-identities", "delete", "-o", "organization1", "-n", "hpc2", "--force");

        assertOutput(format, out, new ManagedIdentityDeleted("organization1", 32L, "hpc2"));
    }

    @Test
    void testDeleteInUse(MockServerClient mock) {
        mockIdentities(mock);
        mock.when(
                request().withMethod("DELETE").withPath("/identities/31").withQueryStringParameter("checked", "true"), exactly(1)
        ).respond(
                response().withStatusCode(409).withBody("{\"managedCredentialsId\":\"41\",\"conflicts\":[{\"type\":\"workflow\",\"id\":\"abc\",\"name\":\"happy_turing\"}]}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "managed-identities", "delete", "-o", "organization1", "-i", "31");

        assertEquals(1, out.exitCode);
        assertTrue(out.stdErr.contains("workflow 'happy_turing' (abc)"), out.stdErr);
        assertTrue(out.stdErr.contains("Use --force"), out.stdErr);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testCredentialsList(OutputType format, MockServerClient mock) throws IOException {
        mockIdentities(mock);
        mockCredentials(mock, null);

        ExecOut out = exec(format, mock, "managed-identities", "credentials", "-o", "organization1", "-n", "hpc1");

        assertOutput(format, out, new ManagedCredentialsList("organization1", "hpc1", List.of(
                parseJson(OWN_ROW, ListManagedCredentialsRespDto.class),
                parseJson(MISSING_ROW, ListManagedCredentialsRespDto.class)
        )));
    }

    @Test
    void testCredentialsListFetchesEveryPage(MockServerClient mock) {
        mockIdentities(mock);
        String fullPage = "{\"managedCredentials\":[" + String.join(",", Collections.nCopies(100, MISSING_ROW)) + "],\"totalSize\":101}";
        mock.when(
                request().withMethod("GET").withPath("/identities/31/credentials").withQueryStringParameter("offset", "0").withQueryStringParameter("max", "100"), exactly(1)
        ).respond(response().withStatusCode(200).withBody(fullPage).withContentType(MediaType.APPLICATION_JSON));
        mock.when(
                request().withMethod("GET").withPath("/identities/31/credentials").withQueryStringParameter("offset", "100").withQueryStringParameter("max", "100"), exactly(1)
        ).respond(response().withStatusCode(200).withBody("{\"managedCredentials\":[" + OWN_ROW + "],\"totalSize\":101}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(OutputType.json, mock, "managed-identities", "credentials", "-o", "organization1", "-n", "hpc1");

        assertEquals(0, out.exitCode, out.stdErr);
        assertEquals(101, out.stdOut.split("\"userName\"", -1).length - 1, out.stdOut);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testCredentialsAddForSelf(OutputType format, MockServerClient mock) throws IOException {
        mockIdentities(mock);
        mock.when(
                request().withMethod("POST").withPath("/identities/31/credentials").withQueryStringParameter("orgId", ORG_ID)
                        .withBody(json("{\"provider\":\"ssh\",\"credentials\":{\"provider\":\"ssh\",\"keys\":{\"privateKey\":\"private_key\",\"passphrase\":\"secret\"}},\"metadata\":{\"userName\":\"jordi_hpc\"}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"managedCredentials\":{\"id\":41}}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "managed-identities", "credentials", "-o", "organization1", "-n", "hpc1",
                "add", "-l", "jordi_hpc", "-k", tempFile("private_key", "id_rsa", ""), "-p", "secret");

        assertOutput(format, out, new ManagedCredentialsAdded("hpc1", 41L));
        mock.verify(request().withMethod("POST").withPath("/identities/31/credentials").withQueryStringParameter("userId", ".*"), org.mockserver.verify.VerificationTimes.exactly(0));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testCredentialsAddForMember(OutputType format, MockServerClient mock) throws IOException {
        mockIdentities(mock);
        mockCredentials(mock, "alice");
        mock.when(
                request().withMethod("POST").withPath("/identities/31/credentials").withQueryStringParameter("orgId", ORG_ID).withQueryStringParameter("userId", "77")
                        .withBody(json("{\"metadata\":{\"userName\":\"alice_hpc\"}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(200).withBody("{\"managedCredentials\":{\"id\":42}}").withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "managed-identities", "credentials", "-o", "organization1", "-i", "31",
                "add", "-m", "alice", "-l", "alice_hpc", "-k", tempFile("private_key", "id_rsa", ""));

        assertOutput(format, out, new ManagedCredentialsAdded("hpc1", 42L));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testCredentialsUpdateForSelf(OutputType format, MockServerClient mock) throws IOException {
        mockIdentities(mock);
        mockCredentials(mock, "jordi");
        mock.when(
                request().withMethod("PUT").withPath("/identities/31/credentials/41").withQueryStringParameter("orgId", ORG_ID)
                        .withBody(json("{\"provider\":\"ssh\",\"credentials\":{\"provider\":\"ssh\",\"keys\":{\"privateKey\":\"new_key\"}},\"metadata\":{\"userName\":\"jordi2\"}}", MatchType.ONLY_MATCHING_FIELDS)),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "managed-identities", "credentials", "-o", "organization1", "-n", "hpc1",
                "update", "-l", "jordi2", "-k", tempFile("new_key", "id_rsa", ""));

        assertOutput(format, out, new ManagedCredentialsUpdated("hpc1", 41L));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testCredentialsDeleteForce(OutputType format, MockServerClient mock) {
        mockIdentities(mock);
        mockCredentials(mock, "jordi");
        mock.when(
                request().withMethod("DELETE").withPath("/identities/31/credentials/41").withQueryStringParameter("orgId", ORG_ID).withQueryStringParameter("checked", "false"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "managed-identities", "credentials", "-o", "organization1", "-n", "hpc1", "delete", "--force");

        assertOutput(format, out, new ManagedCredentialsDeleted("hpc1", 41L));
    }

    @Test
    void testCredentialsDeleteMissing(MockServerClient mock) {
        mockIdentities(mock);
        mockCredentials(mock, "alice");

        ExecOut out = exec(mock, "managed-identities", "credentials", "-o", "organization1", "-n", "hpc1", "delete", "-m", "alice");

        assertEquals(1, out.exitCode);
        assertTrue(out.stdErr.contains("Member 'alice' has no credentials for managed identity 'hpc1'"), out.stdErr);
    }
}
