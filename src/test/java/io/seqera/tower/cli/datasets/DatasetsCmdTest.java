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

package io.seqera.tower.cli.datasets;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.datasets.DatasetCreate;
import io.seqera.tower.cli.responses.datasets.DatasetDelete;
import io.seqera.tower.cli.responses.datasets.DatasetDownload;
import io.seqera.tower.cli.responses.datasets.DatasetList;
import io.seqera.tower.cli.responses.datasets.DatasetUpdate;
import io.seqera.tower.cli.responses.datasets.DatasetUrl;
import io.seqera.tower.cli.responses.datasets.DatasetVersionsList;
import io.seqera.tower.cli.responses.datasets.DatasetView;
import io.seqera.tower.model.Dataset;
import io.seqera.tower.model.DatasetVersionDbDto;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class DatasetsCmdTest extends BaseCmdTest {

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/datasets_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "datasets", "list", "-w", "249664655368293");

        assertOutput(format, out, new DatasetList(Arrays.asList(
                parseJson(" {\n" +
                        "      \"id\": \"4D9TP0w2pM0qmwqVHgrgBK\",\n" +
                        "      \"name\": \"dataset1\",\n" +
                        "      \"description\": null,\n" +
                        "      \"mediaType\": null,\n" +
                        "      \"deleted\": false,\n" +
                        "      \"dateCreated\": \"2021-11-26T14:51:20+01:00\",\n" +
                        "      \"lastUpdated\": \"2021-11-26T14:51:20+01:00\"\n" +
                        "    }", Dataset.class),
                parseJson("{\n" +
                        "      \"id\": \"1W2FqBiI6WoNokQTkPkEzo\",\n" +
                        "      \"name\": \"dataset2\",\n" +
                        "      \"description\": null,\n" +
                        "      \"mediaType\": null,\n" +
                        "      \"deleted\": false,\n" +
                        "      \"dateCreated\": \"2021-11-29T08:05:44+01:00\",\n" +
                        "      \"lastUpdated\": \"2021-11-29T08:05:44+01:00\"\n" +
                        "    }", Dataset.class)
        ), "249664655368293"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "datasets", "view", "-w", "249664655368293", "-i", "4D9TP0w2pM0qmwqVHgrgBK");

        assertOutput(format, out, new DatasetView(parseJson("{\n" +
                "    \"id\": \"4D9TP0w2pM0qmwqVHgrgBK\",\n" +
                "    \"name\": \"dataset1\",\n" +
                "    \"description\": null,\n" +
                "    \"mediaType\": null,\n" +
                "    \"deleted\": false,\n" +
                "    \"dateCreated\": \"2021-11-26T14:51:20+01:00\",\n" +
                "    \"lastUpdated\": \"2021-11-26T14:51:20+01:00\"\n" +
                "  }", Dataset.class), "249664655368293"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testVersions(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/versions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_versions")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "datasets", "view", "-w", "249664655368293", "-i", "4D9TP0w2pM0qmwqVHgrgBK", "versions");

        assertOutput(format, out, new DatasetVersionsList(Arrays.asList(
                parseJson(" {\n" +
                        "      \"datasetId\": \"4D9TP0w2pM0qmwqVHgrgBK\",\n" +
                        "      \"datasetName\": \"dataset1\",\n" +
                        "      \"hasHeader\": false,\n" +
                        "      \"version\": 1,\n" +
                        "      \"lastUpdated\": \"2021-11-29T09:28:09+01:00\",\n" +
                        "      \"fileName\": \"transaciones_2021-11-26_filter-advanced.csv\",\n" +
                        "      \"mediaType\": \"text/csv\",\n" +
                        "      \"url\": \"https://73ab-46-26-157-135.ngrok.io/api/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/v/1/n/transaciones_2021-11-26_filter-advanced.csv\"\n" +
                        "    }", DatasetVersionDbDto.class),
                parseJson("{\n" +
                        "      \"datasetId\": \"4D9TP0w2pM0qmwqVHgrgBK\",\n" +
                        "      \"datasetName\": \"dataset1\",\n" +
                        "      \"hasHeader\": false,\n" +
                        "      \"version\": 2,\n" +
                        "      \"lastUpdated\": \"2021-11-29T09:28:09+01:00\",\n" +
                        "      \"fileName\": \"transaciones_2021-11-26_filter-advanced.csv\",\n" +
                        "      \"mediaType\": \"text/csv\",\n" +
                        "      \"url\": \"https://73ab-46-26-157-135.ngrok.io/api/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/v/2/n/transaciones_2021-11-26_filter-advanced.csv\"\n" +
                        "    }", DatasetVersionDbDto.class)
        ), "4D9TP0w2pM0qmwqVHgrgBK", "249664655368293"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDelete(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "datasets", "delete", "-w", "249664655368293", "-i", "4D9TP0w2pM0qmwqVHgrgBK");

        assertOutput(format, out, new DatasetDelete("4D9TP0w2pM0qmwqVHgrgBK", "249664655368293"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUrl(OutputType format, MockServerClient mock) throws JsonProcessingException {

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/versions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_versions")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "datasets", "url", "-w", "249664655368293", "-i", "4D9TP0w2pM0qmwqVHgrgBK");

        assertOutput(format, out, new DatasetUrl("https://73ab-46-26-157-135.ngrok.io/api/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/v/2/n/transaciones_2021-11-26_filter-advanced.csv", "4D9TP0w2pM0qmwqVHgrgBK", "249664655368293"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);

    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDownload(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/versions"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_versions")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/v/2/n/transaciones_2021-11-26_filter-advanced.csv"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_download", "txt")).withContentType(MediaType.TEXT_PLAIN)
        );

        ExecOut out = exec(format, mock, "datasets", "download", "-w", "249664655368293", "-i", "4D9TP0w2pM0qmwqVHgrgBK");

        String path = tempFile(new String(loadResource("datasets/dataset_download", "txt"), StandardCharsets.UTF_8), "data", ".txt");

        File file = new File(path);

        assertOutput(format, out, new DatasetDownload(file, "transaciones_2021-11-26_filter-advanced.csv"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAdd(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("POST").withPath("/workspaces/249664655368293/datasets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_created_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST")
                        .withPath("/workspaces/249664655368293/datasets/1W3BTHWgRH71OJmOPMdG7S/upload")
                        .withQueryStringParameter("header", "false")
                , exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_upload_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "datasets", "add", tempFile(new String(loadResource("datasets/dataset_data", "csv"), StandardCharsets.UTF_8), "data", ".csv"), "-w", "249664655368293", "-n", "dataset3", "-d", "Dataset 3 description.");

        assertOutput(format, out, new DatasetCreate("dataset3", "249664655368293", "1W3BTHWgRH71OJmOPMdG7S"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testAddWithOverwrite(OutputType format, MockServerClient mock) throws IOException {

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/datasets_list")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("POST").withPath("/workspaces/249664655368293/datasets"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(JsonBody.json("{\n" +
                        "  \"dataset\": {\n" +
                        "    \"id\": \"1W3BTHWgRH71OJmOPMdG7S\",\n" +
                        "    \"name\": \"dataset2\",\n" +
                        "    \"description\": \"Dataset 2 description.\",\n" +
                        "    \"mediaType\": null,\n" +
                        "    \"deleted\": false,\n" +
                        "    \"dateCreated\": \"2021-11-29T11:18:06.108+01:00\",\n" +
                        "    \"lastUpdated\": \"2021-11-29T11:18:06.108+01:00\"\n" +
                        "  }\n" +
                        "}"))
        );

        mock.when(
                request().withMethod("POST")
                        .withPath("/workspaces/249664655368293/datasets/1W3BTHWgRH71OJmOPMdG7S/upload")
                        .withQueryStringParameter("header", "false")
                , exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_upload_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("DELETE").withPath("/workspaces/249664655368293/datasets/1W2FqBiI6WoNokQTkPkEzo"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "datasets", "add", "--overwrite", tempFile(new String(loadResource("datasets/dataset_data", "csv"), StandardCharsets.UTF_8), "data", ".csv"), "-w", "249664655368293", "-n", "dataset2", "-d", "Dataset 2 description.");

        assertOutput(format, out, new DatasetCreate("dataset2", "249664655368293", "1W3BTHWgRH71OJmOPMdG7S"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdate(OutputType format, MockServerClient mock) {
        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        ExecOut out = exec(format, mock, "datasets", "update", "-w", "249664655368293", "-i", "4D9TP0w2pM0qmwqVHgrgBK", "--new-name", "dataset1", "-d", "Dataset 3 description.");

        assertOutput(format, out, new DatasetUpdate("dataset1", "249664655368293", "4D9TP0w2pM0qmwqVHgrgBK"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpdateWithFile(OutputType format, MockServerClient mock) throws IOException {
        mock.when(
                request().withMethod("GET").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/metadata"), exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_metadata")).withContentType(MediaType.APPLICATION_JSON)
        );

        mock.when(
                request().withMethod("PUT").withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK"), exactly(1)
        ).respond(
                response().withStatusCode(204)
        );

        mock.when(
                request().withMethod("POST")
                        .withPath("/workspaces/249664655368293/datasets/4D9TP0w2pM0qmwqVHgrgBK/upload")
                        .withQueryStringParameter("header", "false")
                , exactly(1)
        ).respond(
                response().withStatusCode(200).withBody(loadResource("datasets/dataset_upload_response")).withContentType(MediaType.APPLICATION_JSON)
        );

        ExecOut out = exec(format, mock, "datasets", "update", "-w", "249664655368293", "-i", "4D9TP0w2pM0qmwqVHgrgBK", "--new-name", "dataset1", "-d", "Dataset 3 description.", "-f", tempFile(new String(loadResource("datasets/dataset_data", "csv"), StandardCharsets.UTF_8), "data", ".csv"));

        assertOutput(format, out, new DatasetUpdate("dataset1", "249664655368293", "4D9TP0w2pM0qmwqVHgrgBK"));
        assertEquals("", out.stdErr);
        assertEquals(0, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testFileNotExistsError(OutputType format, MockServerClient mock) throws IOException {

        ExecOut out = exec(format, mock, "datasets", "add", "-w", "249664655368293", "-n", "name", "path/that/do/not/exist/file.tsv");

        assertEquals(errorMessage(out.app, new TowerException(String.format("File path '%s' do not exists.", Path.of("path/that/do/not/exist/file.tsv")))), out.stdErr);
        assertEquals("", out.stdOut);
        assertEquals(1, out.exitCode);
    }
}
