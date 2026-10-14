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


package io.seqera.tower.cli.auditlogs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.BaseCmdTest;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.responses.auditlogs.AuditLogView;
import io.seqera.tower.cli.responses.auditlogs.AuditLogsExported;
import io.seqera.tower.cli.responses.auditlogs.AuditLogsList;
import io.seqera.tower.model.AuditLogV2ResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class AuditLogsCmdTest extends BaseCmdTest {

    private static final String LOG = """
            {
              "id": "1001",
              "timestamp": "2026-09-01T10:00:00Z",
              "event": "agent_created",
              "correlationId": "corr-1",
              "actor": {"type": "user", "userId": 1264, "userName": "jordi", "email": "jordi@seqera.io"},
              "client": {"ip": "10.0.0.1", "userAgent": "tw/0.42.0"},
              "target": {"type": "agent", "id": "agt_1", "name": "fix-failed-runs",
                         "organization": {"id": 27736513644467, "name": "organization1"},
                         "workspace": {"id": 75887156211589, "name": "workspace1"}}
            }
            """;

    private static final String CSV = "id,timestamp,event\n1001,2026-09-01T10:00:00Z,agent_created\n";

    @BeforeEach
    void init(MockServerClient mock) {
        mock.reset();
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testList(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(request().withMethod("GET").withPath("/admin/audit-logs-v2")
                        .withQueryStringParameter("timestampAfterOrEqual", "2026-09-01T00:00:00Z")
                        .withQueryStringParameter("timestampBeforeOrEqual", "2026-09-30T23:59:59Z")
                        .withQueryStringParameter("max", "1")
                        .withQueryStringParameter("nextPageToken", "tok1"), exactly(1))
                .respond(response().withStatusCode(200).withBody("{\"auditLogs\":[" + LOG + "],\"nextPageToken\":\"tok2\"}").withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "audit-logs", "list", "--after", "2026-09-01T00:00:00Z", "--before", "2026-09-30T23:59:59Z", "--max", "1", "--page-token", "tok1");

        assertOutput(format, out, new AuditLogsList(List.of(parseJson(LOG, AuditLogV2ResponseDto.class)), "tok2"));
    }

    @Test
    void testListRejectsInvalidDate(MockServerClient mock) {
        ExecOut out = exec(mock, "audit-logs", "list", "--after", "yesterday");

        assertEquals(2, out.exitCode);
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testView(OutputType format, MockServerClient mock) throws JsonProcessingException {
        mock.when(request().withMethod("GET").withPath("/admin/audit-logs-v2/1001"), exactly(1))
                .respond(response().withStatusCode(200).withBody(LOG).withContentType(MediaType.APPLICATION_JSON));

        ExecOut out = exec(format, mock, "audit-logs", "view", "-i", "1001");

        assertOutput(format, out, new AuditLogView(parseJson(LOG, AuditLogV2ResponseDto.class)));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testExportToStdout(OutputType format, MockServerClient mock) {
        mock.when(request().withMethod("GET").withPath("/admin/audit-logs-v2/export-csv")
                        .withQueryStringParameter("timestampAfterOrEqual", "2026-09-01T00:00:00Z"), exactly(1))
                .respond(response().withStatusCode(200).withBody(CSV).withContentType(MediaType.parse("text/csv"))
                        .withHeader("Content-Disposition", "attachment; filename=\"audit-logs.csv\""));

        ExecOut out = exec(format, mock, "audit-logs", "export", "--after", "2026-09-01T00:00:00Z");

        assertOutput(format, out, new AuditLogsExported(null, CSV));
    }

    @ParameterizedTest
    @EnumSource(OutputType.class)
    void testExportToFile(OutputType format, MockServerClient mock) throws IOException {
        mock.when(request().withMethod("GET").withPath("/admin/audit-logs-v2/export-csv")
                        .withQueryStringParameter("attributes", "state"), exactly(1))
                .respond(response().withStatusCode(200).withBody(CSV).withContentType(MediaType.parse("text/csv"))
                        .withHeader("Content-Disposition", "attachment; filename=\"audit-logs.csv\""));

        Path file = tempDir().resolve("export.csv");
        ExecOut out = exec(format, mock, "audit-logs", "export", "--state", "-o", file.toString());

        assertOutput(format, out, new AuditLogsExported(file.toString(), null));
        assertEquals(CSV, Files.readString(file));
    }
}
