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

package io.seqera.tower.cli.pipelines;

import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.PipelineNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.pipelines.versions.ListPipelineVersionsCmdResponse;
import io.seqera.tower.cli.responses.pipelines.versions.UpdatePipelineVersionCmdResponse;
import io.seqera.tower.cli.responses.pipelines.versions.ViewPipelineVersionCmdResponse;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.time.OffsetDateTime;
import java.util.List;

import static io.seqera.tower.cli.commands.AbstractApiCmd.USER_WORKSPACE_NAME;
import static org.apache.commons.lang3.StringUtils.chop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

class PipelineVersionsCmdTest extends BaseCmdTest {

    private static final Long PIPELINE_ID = 188439584587120L;
    private static final String PIPELINE_NAME = "TestVersioningInUserWsp";

    private static final String HASH_V1 = "JHY1OjIzYjNmYmVkN2NhZTU4Y2U0NDk1ZjA2MDY4YWRlOTE2MzJlMWFkMjlhY2RkNjY0NDM0MzFlMzY3NGEzNTBmNWMyOTIxMjhhMjNiMDMxMWU2ZjY2MmY4OTQ2OGVjOTRlMGNjMDVkNThkYTc2OGE2ZjVhNDlmY2JhZjY3YjNjYzY1";
    private static final String HASH_V2 = "JHY1OjU1MmEyZDEzZDI1MjA1MjJlNzc4MjdkM2M3ZmM2ZjdiMzhhYmMwNWEwZjNjYWM4MjlmYjI3MzU0MjNkNWI5YWQyNWVmYWFjNjQyNjUzNWQ5OGNlOTA5MWY1OTI3Yzg1OTk4MzAyYWM2ZTk1MzNhYzJmMjQzNGJiZTBkNjQ3MTg1";
    private static final String HASH_DRAFT = "JHY1OjdlYmZmODY1MzUwMWRmNjJlMDc0YjIwNGY4MTExYTIwNzRmNTU2MzFjZjg4YTA1ODk1ZTAwMTM1NWUzMGQzZjZmOGQ4MGRhMTY5NTFmNTc3NWViMGYwYWYyZDM4NTBiYzZhZTcwODU3YTkyZWIyOGFiNjA2M2I4N2I4MWQ5MTlh";

    private static final String VERSION_ID_V1 = "7TnlaOKANkiDIdDqOO2kCs";

    private List<PipelineVersionFullInfoDto> allVersions() {
        return List.of(
                new PipelineVersionFullInfoDto()
                        .id("7TnlaOKANkiDIdDqOO2kCs")
                        .name("TestVersioningInUserWsp-1")
                        .hash(HASH_V1)
                        .isDefault(true)
                        .creatorUserName("jaime-munoz")
                        .creatorUserId(1L)
                        .dateCreated(OffsetDateTime.parse("2026-02-17T17:43:32Z"))
                        .lastUpdated(OffsetDateTime.parse("2026-02-17T17:43:32Z")),
                new PipelineVersionFullInfoDto()
                        .id("a48GJwfXIUUPIakcwFeue")
                        .name("TestVersioningInUserWsp-2")
                        .hash(HASH_V2)
                        .isDefault(false)
                        .creatorUserName("jaime-munoz")
                        .creatorUserId(1L)
                        .dateCreated(OffsetDateTime.parse("2026-02-17T18:27:39Z"))
                        .lastUpdated(OffsetDateTime.parse("2026-02-17T18:27:39Z")),
                new PipelineVersionFullInfoDto()
                        .id("7KtabH1PaW1IBPYUdzVcXh")
                        .name(null)
                        .hash(HASH_DRAFT)
                        .isDefault(false)
                        .creatorUserName("jaime-munoz")
                        .creatorUserId(1L)
                        .dateCreated(OffsetDateTime.parse("2026-02-17T18:28:01Z"))
                        .lastUpdated(OffsetDateTime.parse("2026-02-17T18:28:01Z"))
        );
    }

    private List<PipelineVersionFullInfoDto> publishedVersions() {
        return List.of(
                new PipelineVersionFullInfoDto()
                        .id("7TnlaOKANkiDIdDqOO2kCs")
                        .name("TestVersioningInUserWsp-1")
                        .hash(HASH_V1)
                        .isDefault(true)
                        .creatorUserName("jaime-munoz")
                        .creatorUserId(1L)
                        .dateCreated(OffsetDateTime.parse("2026-02-17T17:43:32Z"))
                        .lastUpdated(OffsetDateTime.parse("2026-02-17T17:43:32Z")),
                new PipelineVersionFullInfoDto()
                        .id("a48GJwfXIUUPIakcwFeue")
                        .name("TestVersioningInUserWsp-2")
                        .hash(HASH_V2)
                        .isDefault(false)
                        .creatorUserName("jaime-munoz")
                        .creatorUserId(1L)
                        .dateCreated(OffsetDateTime.parse("2026-02-17T18:27:39Z"))
                        .lastUpdated(OffsetDateTime.parse("2026-02-17T18:27:39Z"))
        );
    }

    private void mockPipelineSearchByName(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/pipelines")
                        .withQueryStringParameter("search", "\"" + PIPELINE_NAME + "\"")
                        .withQueryStringParameter("visibility", "all"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody(loadResource("pipeline_versions/pipelines_search"))
                        .withContentType(MediaType.APPLICATION_JSON)
        );
    }

    private void mockPipelineDescribe(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody(loadResource("pipeline_versions/pipeline_describe"))
                        .withContentType(MediaType.APPLICATION_JSON)
        );
    }

    private void mockVersionsList(MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID + "/versions"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody(loadResource("pipeline_versions/versions_list"))
                        .withContentType(MediaType.APPLICATION_JSON)
        );
    }

    private void mockManageVersion(MockServerClient mock, String expectedBody) {
        mock.when(
                request().withMethod("PUT").withPath("/pipelines/" + PIPELINE_ID + "/versions/" + VERSION_ID_V1 + "/manage")
                        .withBody(json(expectedBody)),
                exactly(1)
        ).respond(
                response().withStatusCode(204)
        );
    }

    // --- List command tests ---

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testListVersionsByName(OutputType format, MockServerClient mock) {

        mock.reset();
        mockPipelineSearchByName(mock);
        mockPipelineDescribe(mock);
        mockVersionsList(mock);

        ExecOut out = exec(format, mock, "pipelines", "versions", "list", "-n", PIPELINE_NAME);

        assertOutput(format, out, new ListPipelineVersionsCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME,
                allVersions(), PaginationInfo.from((Integer) null, (Integer) null), false
        ));
    }

    @Test
    void testListVersionsByPipelineId(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);
        mockVersionsList(mock);

        ExecOut out = exec(mock, "pipelines", "versions", "list", "-i", PIPELINE_ID.toString());

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(chop(new ListPipelineVersionsCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME,
                allVersions(), PaginationInfo.from((Integer) null, (Integer) null), false
        ).toString()), out.stdOut);
    }

    @Test
    void testListVersionsEmpty(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);

        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID + "/versions"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody("{\"versions\":[],\"totalSize\":0}")
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "list", "-i", PIPELINE_ID.toString());

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(chop(new ListPipelineVersionsCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME,
                List.of(), PaginationInfo.from((Integer) null, (Integer) null), false
        ).toString()), out.stdOut);
    }

    @Test
    void testListVersionsWithFilter(MockServerClient mock) {

        mock.reset();
        mockPipelineSearchByName(mock);
        mockPipelineDescribe(mock);

        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID + "/versions")
                        .withQueryStringParameter("search", "TestVersioningInUserWsp-1")
                        .withQueryStringParameter("isPublished", "true"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody(loadResource("pipeline_versions/versions_published"))
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "list", "-n", PIPELINE_NAME, "-f", "TestVersioningInUserWsp-1");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @Test
    void testListVersionsWithPagination(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);

        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID + "/versions")
                        .withQueryStringParameter("max", "2")
                        .withQueryStringParameter("offset", "1"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody(loadResource("pipeline_versions/versions_published"))
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "list", "-i", PIPELINE_ID.toString(), "--offset", "1", "--max", "2");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(chop(new ListPipelineVersionsCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME,
                publishedVersions(), PaginationInfo.from(1, 2), false
        ).toString()), out.stdOut);
    }

    @Test
    void testListVersionsPipelineNotFound(MockServerClient mock) {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/pipelines")
                        .withQueryStringParameter("search", "\"nonexistent\"")
                        .withQueryStringParameter("visibility", "all"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody("{\"pipelines\":[],\"totalSize\":0}")
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "list", "-n", "nonexistent");

        assertEquals(errorMessage(out.app, new PipelineNotFoundException("\"nonexistent\"", USER_WORKSPACE_NAME)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testListVersionsFeatureDisabled(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);

        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID + "/versions"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody("{\"versions\":null,\"totalSize\":0}")
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "list", "-i", PIPELINE_ID.toString());

        assertEquals(errorMessage(out.app, new TowerException("No versions available for the pipeline, check if Pipeline versioning feature is enabled for the workspace")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testListVersionsWithFullHash(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);
        mockVersionsList(mock);

        ExecOut out = exec(mock, "pipelines", "versions", "list", "-i", PIPELINE_ID.toString(), "--full-hash");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(chop(new ListPipelineVersionsCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME,
                allVersions(), PaginationInfo.from((Integer) null, (Integer) null), true
        ).toString()), out.stdOut);
    }

    // --- View command tests ---

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testViewVersionById(OutputType format, MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);
        mockVersionsList(mock);

        ExecOut out = exec(format, mock, "pipelines", "versions", "view", "-i", PIPELINE_ID.toString(), "--version-id", VERSION_ID_V1);

        assertOutput(format, out, new ViewPipelineVersionCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME, allVersions().get(0)
        ));
    }

    @Test
    void testViewVersionByName(MockServerClient mock) {

        mock.reset();
        mockPipelineSearchByName(mock);
        mockPipelineDescribe(mock);

        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID + "/versions")
                        .withQueryStringParameter("search", "TestVersioningInUserWsp-2")
                        .withQueryStringParameter("isPublished", "true"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody(loadResource("pipeline_versions/versions_published"))
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "view", "-n", PIPELINE_NAME, "--version-name", "TestVersioningInUserWsp-2");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(chop(new ViewPipelineVersionCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME, publishedVersions().get(1)
        ).toString()), out.stdOut);
    }

    @Test
    void testViewVersionNotFound(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);
        mockVersionsList(mock);

        ExecOut out = exec(mock, "pipelines", "versions", "view", "-i", PIPELINE_ID.toString(), "--version-id", "nonexistent");

        assertEquals(errorMessage(out.app, new TowerException("Pipeline version 'nonexistent' not found")), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }

    @Test
    void testViewDraftVersionById(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);
        mockVersionsList(mock);

        ExecOut out = exec(mock, "pipelines", "versions", "view", "-i", PIPELINE_ID.toString(), "--version-id", "7KtabH1PaW1IBPYUdzVcXh");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(chop(new ViewPipelineVersionCmdResponse(
                null, PIPELINE_ID, PIPELINE_NAME, allVersions().get(2)
        ).toString()), out.stdOut);
    }

    // --- Update command tests ---

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateVersionName(OutputType format, MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);
        mockManageVersion(mock, "{\"name\":\"new-version-name\"}");

        ExecOut out = exec(format, mock, "pipelines", "versions", "update", "-i", PIPELINE_ID.toString(),
                "--version-id", VERSION_ID_V1, "--new-name", "new-version-name");

        assertOutput(format, out, new UpdatePipelineVersionCmdResponse(null, PIPELINE_ID, PIPELINE_NAME, VERSION_ID_V1));
    }

    @Test
    void testUpdateVersionSetDefault(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);
        mockManageVersion(mock, "{\"isDefault\":true}");

        ExecOut out = exec(mock, "pipelines", "versions", "update", "-i", PIPELINE_ID.toString(),
                "--version-id", VERSION_ID_V1, "--set-default");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new UpdatePipelineVersionCmdResponse(null, PIPELINE_ID, PIPELINE_NAME, VERSION_ID_V1).toString(), out.stdOut);
    }

    @Test
    void testUpdateVersionByPipelineName(MockServerClient mock) {

        mock.reset();
        mockPipelineSearchByName(mock);
        mockPipelineDescribe(mock);
        mockManageVersion(mock, "{\"name\":\"renamed-version\"}");

        ExecOut out = exec(mock, "pipelines", "versions", "update", "-n", PIPELINE_NAME,
                "--version-id", VERSION_ID_V1, "--new-name", "renamed-version");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new UpdatePipelineVersionCmdResponse(null, PIPELINE_ID, PIPELINE_NAME, VERSION_ID_V1).toString(), out.stdOut);
    }

    @Test
    void testUpdateVersionByVersionName(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);

        // Mock versions list endpoint for version name resolution
        mock.when(
                request().withMethod("GET").withPath("/pipelines/" + PIPELINE_ID + "/versions")
                        .withQueryStringParameter("search", "TestVersioningInUserWsp-1")
                        .withQueryStringParameter("isPublished", "true"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody(loadResource("pipeline_versions/versions_published"))
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        mockManageVersion(mock, "{\"name\":\"renamed\"}");

        ExecOut out = exec(mock, "pipelines", "versions", "update", "-i", PIPELINE_ID.toString(),
                "--version-name", "TestVersioningInUserWsp-1", "--new-name", "renamed");

        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
        assertEquals(new UpdatePipelineVersionCmdResponse(null, PIPELINE_ID, PIPELINE_NAME, VERSION_ID_V1).toString(), out.stdOut);
    }

    @Test
    void testUpdateVersionInvalidName(MockServerClient mock) {

        mock.reset();
        mockPipelineDescribe(mock);

        mock.when(
                request().withMethod("PUT").withPath("/pipelines/" + PIPELINE_ID + "/versions/" + VERSION_ID_V1 + "/manage")
                        .withBody(json("{\"name\":\"!invalid!\"}")),
                exactly(1)
        ).respond(
                response().withStatusCode(400)
                        .withBody("{\"message\":\"Invalid pipeline version name: must match pattern [a-zA-Z\\\\d][-._a-zA-Z\\\\d]{1,108}\"}")
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "update", "-i", PIPELINE_ID.toString(),
                "--version-id", VERSION_ID_V1, "--new-name", "!invalid!");

        assertEquals(1, out.exitCode);
        assertEquals("", out.stdOut);
    }

    @Test
    void testUpdateVersionPipelineNotFound(MockServerClient mock) {

        mock.reset();

        mock.when(
                request().withMethod("GET").withPath("/pipelines")
                        .withQueryStringParameter("search", "\"nonexistent\"")
                        .withQueryStringParameter("visibility", "all"),
                exactly(1)
        ).respond(
                response().withStatusCode(200)
                        .withBody("{\"pipelines\":[],\"totalSize\":0}")
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(mock, "pipelines", "versions", "update", "-n", "nonexistent",
                "--version-id", VERSION_ID_V1, "--new-name", "new-name");

        assertEquals(errorMessage(out.app, new PipelineNotFoundException("\"nonexistent\"", USER_WORKSPACE_NAME)), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }
}
