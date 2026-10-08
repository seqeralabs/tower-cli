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


package io.seqera.tower.cli.responses.auditlogs;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.AuditLogV2ResponseDto;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;

public class AuditLogsList extends Response {

    public final List<AuditLogV2ResponseDto> auditLogs;
    public final String nextPageToken;

    public AuditLogsList(List<AuditLogV2ResponseDto> auditLogs, String nextPageToken) {
        this.auditLogs = auditLogs;
        this.nextPageToken = nextPageToken;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Audit logs:|@%n")));
        if (auditLogs == null || auditLogs.isEmpty()) {
            out.println(ansi("    @|yellow No audit logs found|@"));
            return;
        }

        TableList table = new TableList(out, 6, "ID", "Timestamp", "Event", "Actor", "Target type", "Target name");
        table.setPrefix("    ");
        auditLogs.forEach(log -> table.addRow(
                log.getId(),
                formatDate(log.getTimestamp()),
                log.getEvent() == null ? "" : log.getEvent().getValue(),
                log.getActor() == null ? "" : Objects.toString(log.getActor().getUserName(), ""),
                log.getTarget() == null ? "" : Objects.toString(log.getTarget().getType(), ""),
                log.getTarget() == null ? "" : Objects.toString(log.getTarget().getName(), "")
        ));
        table.print();
        if (nextPageToken != null) {
            out.println(String.format("%n  More logs available, use: --page-token %s", nextPageToken));
        }
        out.println("");
    }
}
