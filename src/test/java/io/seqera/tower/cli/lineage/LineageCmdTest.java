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


package io.seqera.tower.cli.lineage;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.lineage.LineageRecordView;
import io.seqera.tower.cli.responses.lineage.LineageRecordsList;
import io.seqera.tower.cli.responses.lineage.LineageResolved;
import io.seqera.tower.model.LineageRecordResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class LineageCmdTest extends BaseCmdTest {

    private static final String WSP_ID = "75887156211589";
    private static final String WSP_REF = "[organization1 / workspace1]";
    private static final String LID = "lid://abc123/multiqc report.html";
    // The LID is sent double-encoded, as the web UI does: the API decodes it twice back to LID
    private static final String LID_PATH = "lid%253A%252F%252Fabc123%252Fmultiqc%2Breport.html";

    private static final String FILE_RECORD = """
            {
              "lid": "lid://abc123/multiqc report.html",
              "type": "FileOutput",
              "recordUri": "s3://bucket/lineage/abc123/multiqc report.html.data.json",
              "data": {"path": "s3://bucket/results/multiqc report.html", "labels": ["qc"]},
              "displayData": {"workspaceId": 75887156211589, "workflowId": "4abc", "runName": "happy_turing", "processName": "MULTIQC"}
            }
            """;

    private static final String TASK_RECORD = """
            {
              "lid": "lid://def456",
              "type": "TaskRun",
              "displayData": {"workspaceId": 75887156211589, "workflowId": "4abc", "runName": "happy_turing", "processName": "MULTIQC"}
            }
            """;

    @BeforeEach
    void init(MockServerClient mock) {
        mock.reset();
    }

    private void mockWorkspace(MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath("/user-info"), exactly(1))
                .respond(response().withStatusCode(200).withBody(loadResource("user")).withContentType(MediaType.APPLICATION_JSON));
        mock.when(request().withMethod("GET").withPath("/user/1264/workspaces"), exactly(1))
                .respond(response().withStatusCode(200).withBody(loadResource("workspaces/workspaces_list")).withContentType(MediaType.APPLICATION_JSON));
    }

    private void assertSentRawPath(MockServerClient mock, String rawPath) {
        String sent = mock.retrieveRecordedRequests(request().withMethod("GET").withPath("/lineage/.*"))[0].getPath().getValue();
        assertEquals(rawPath, sent);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testResolveRun(OutputType format, MockServerClient mock) {
        mockWorkspace(mock);
        mock.when(request().withMethod("GET").withPath("/lineage/resolve")
                        .withQueryStringParameter("workspaceId", WSP_ID)
                        .withQueryStringParameter("sessionId", "f1e2d3"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"lid\":\"lid://wf789\"}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "lineage", "resolve", "-w", WSP_ID, "--session-id", "f1e2d3");

        assertOutput(format, out, new LineageResolved(WSP_REF, "lid://wf789"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testResolveFilePath(OutputType format, MockServerClient mock) {
        mockWorkspace(mock);
        mock.when(request().withMethod("GET").withPath("/lineage/resolve")
                        .withQueryStringParameter("workspaceId", WSP_ID)
                        .withQueryStringParameter("filePath", "s3://bucket/results/report.html"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"lid\":\"lid://abc123/report.html\"}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "lineage", "resolve", "-w", WSP_ID, "--file-path", "s3://bucket/results/report.html");

        assertOutput(format, out, new LineageResolved(WSP_REF, "lid://abc123/report.html"));
    }

    @Test
    void testResolveRejectsRunAndFilePath(MockServerClient mock) {
        ExecOut out = exec(mock, "lineage", "resolve", "-w", WSP_ID, "--session-id", "f1e2d3", "--file-path", "/data/x");

        assertEquals(2, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mockWorkspace(mock);
        mock.when(request().withMethod("GET").withPath("/lineage/.*").withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(200).withBody(FILE_RECORD).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "lineage", "view", "-w", WSP_ID, "-i", LID);

        assertSentRawPath(mock, "/lineage/" + LID_PATH);
        assertOutput(format, out, new LineageRecordView(WSP_REF, parseJson(FILE_RECORD, LineageRecordResponse.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testViewSummary(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mockWorkspace(mock);
        mock.when(request().withMethod("GET").withPath("/lineage/.*/summary").withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(200).withBody(FILE_RECORD).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "lineage", "view", "-w", WSP_ID, "-i", LID, "--summary");

        assertSentRawPath(mock, "/lineage/" + LID_PATH + "/summary");
        assertOutput(format, out, new LineageRecordView(WSP_REF, parseJson(FILE_RECORD, LineageRecordResponse.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testSearch(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(request().withMethod("GET").withPath("/lineage/search")
                        .withQueryStringParameter("q", "type:FileOutput workspace:organization1/workspace1 multiqc")
                        .withQueryStringParameter("pageToken", "tok1")
                        .withQueryStringParameter("pageSize", "2"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"records\":[" + FILE_RECORD + "],\"nextPageToken\":\"tok2\"}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "lineage", "search", "-q", "type:FileOutput workspace:organization1/workspace1 multiqc", "--page-token", "tok1", "--max", "2");

        assertOutput(format, out, new LineageRecordsList("Lineage records", List.of(parseJson(FILE_RECORD, LineageRecordResponse.class)), "tok2"));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testUpstream(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mockWorkspace(mock);
        mock.when(request().withMethod("GET").withPath("/lineage/.*/upstream")
                        .withQueryStringParameter("workspaceId", WSP_ID)
                        .withQueryStringParameter("type", "TaskRun"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"records\":[" + TASK_RECORD + "]}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "lineage", "upstream", "-w", WSP_ID, "-i", LID, "--type", "taskrun");

        assertSentRawPath(mock, "/lineage/" + LID_PATH + "/upstream");
        assertOutput(format, out, new LineageRecordsList("Lineage records upstream of '" + LID + "'", List.of(parseJson(TASK_RECORD, LineageRecordResponse.class)), null));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testDownstream(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mockWorkspace(mock);
        mock.when(request().withMethod("GET").withPath("/lineage/.*/downstream")
                        .withQueryStringParameter("workspaceId", WSP_ID), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"records\":[" + FILE_RECORD + "]}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "lineage", "downstream", "-w", WSP_ID, "-i", "lid://def456");

        assertSentRawPath(mock, "/lineage/lid%253A%252F%252Fdef456/downstream");
        assertOutput(format, out, new LineageRecordsList("Lineage records downstream of 'lid://def456'", List.of(parseJson(FILE_RECORD, LineageRecordResponse.class)), null));
    }
}
