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


package io.seqera.tower.cli.responses.lineage;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DisplayData;
import io.seqera.tower.model.LineageRecordResponse;

import java.io.PrintWriter;
import java.util.Objects;

import static io.seqera.tower.cli.utils.JsonHelper.prettyJson;

public class LineageRecordView extends Response {

    public final String workspaceRef;
    public final LineageRecordResponse record;

    public LineageRecordView(String workspaceRef, LineageRecordResponse record) {
        this.workspaceRef = workspaceRef;
        this.record = record;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Lineage record at %s workspace:|@%n", workspaceRef)));
        DisplayData display = record.getDisplayData() == null ? new DisplayData() : record.getDisplayData();
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("LID", record.getLid());
        table.addRow("Type", record.getType());
        table.addRow("Record URI", Objects.toString(record.getRecordUri(), ""));
        table.addRow("Run ID", Objects.toString(display.getWorkflowId(), ""));
        table.addRow("Run name", Objects.toString(display.getRunName(), ""));
        table.addRow("Process", Objects.toString(display.getProcessName(), ""));
        table.addRow("Pipeline", Objects.toString(display.getPipelineName(), ""));
        table.print();

        if (record.getData() != null) {
            out.println(ansi(String.format("%n  @|bold Data:|@%n")));
            try {
                out.println(prettyJson(record.getData()));
            } catch (JsonProcessingException e) {
                throw new TowerRuntimeException("Unable to format the lineage record data: " + e.getMessage());
            }
        }
        out.println("");
    }
}
