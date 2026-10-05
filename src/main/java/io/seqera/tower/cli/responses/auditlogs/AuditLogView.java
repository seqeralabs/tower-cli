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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.AuditLogV2ResponseDto;
import io.seqera.tower.model.AuditLogV2ResponseDtoActor;
import io.seqera.tower.model.AuditLogV2ResponseDtoClient;
import io.seqera.tower.model.AuditLogV2ResponseDtoTarget;

import java.io.PrintWriter;
import java.util.Objects;

import static io.seqera.tower.cli.utils.FormatHelper.formatDate;
import static io.seqera.tower.cli.utils.JsonHelper.prettyJson;

public class AuditLogView extends Response {

    public final AuditLogV2ResponseDto auditLog;

    public AuditLogView(AuditLogV2ResponseDto auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public void toString(PrintWriter out) {
        AuditLogV2ResponseDtoActor actor = auditLog.getActor() == null ? new AuditLogV2ResponseDtoActor() : auditLog.getActor();
        AuditLogV2ResponseDtoClient client = auditLog.getClient() == null ? new AuditLogV2ResponseDtoClient() : auditLog.getClient();
        AuditLogV2ResponseDtoTarget target = auditLog.getTarget() == null ? new AuditLogV2ResponseDtoTarget() : auditLog.getTarget();

        out.println(ansi(String.format("%n  @|bold Audit log:|@%n")));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", auditLog.getId());
        table.addRow("Timestamp", formatDate(auditLog.getTimestamp()));
        table.addRow("Event", auditLog.getEvent() == null ? "" : auditLog.getEvent().getValue());
        table.addRow("Correlation ID", Objects.toString(auditLog.getCorrelationId(), ""));
        table.addRow("Actor", Objects.toString(actor.getUserName(), ""));
        table.addRow("Actor email", Objects.toString(actor.getEmail(), ""));
        table.addRow("Actor type", actor.getType() == null ? "" : actor.getType().getValue());
        table.addRow("Client IP", Objects.toString(client.getIp(), ""));
        table.addRow("User agent", Objects.toString(client.getUserAgent(), ""));
        table.addRow("Target type", Objects.toString(target.getType(), ""));
        table.addRow("Target ID", Objects.toString(target.getId(), ""));
        table.addRow("Target name", Objects.toString(target.getName(), ""));
        table.addRow("Organization", target.getOrganization() == null ? "" : Objects.toString(target.getOrganization().getName(), ""));
        table.addRow("Workspace", target.getWorkspace() == null ? "" : Objects.toString(target.getWorkspace().getName(), ""));
        table.print();

        if (target.getState() != null) {
            out.println(ansi(String.format("%n  @|bold Target state:|@%n")));
            try {
                out.println(prettyJson(target.getState()));
            } catch (JsonProcessingException e) {
                throw new TowerRuntimeException("Unable to format the audit log state: " + e.getMessage());
            }
        }
        out.println("");
    }
}
