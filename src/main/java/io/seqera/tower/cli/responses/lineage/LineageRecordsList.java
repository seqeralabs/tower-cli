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

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DisplayData;
import io.seqera.tower.model.LineageRecordResponse;

import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

public class LineageRecordsList extends Response {

    public final String title;
    public final List<LineageRecordResponse> records;
    public final String nextPageToken;

    public LineageRecordsList(String title, List<LineageRecordResponse> records, String nextPageToken) {
        this.title = title;
        this.records = records;
        this.nextPageToken = nextPageToken;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold %s:|@%n", title)));
        if (records == null || records.isEmpty()) {
            out.println(ansi("    @|yellow No lineage records found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "LID", "Type", "Run name", "Process", "Workspace ID");
        table.setPrefix("    ");
        records.forEach(record -> {
            DisplayData display = record.getDisplayData() == null ? new DisplayData() : record.getDisplayData();
            table.addRow(
                    record.getLid(),
                    record.getType(),
                    Objects.toString(display.getRunName(), ""),
                    Objects.toString(display.getProcessName(), ""),
                    Objects.toString(display.getWorkspaceId(), "")
            );
        });
        table.print();
        if (nextPageToken != null) {
            out.println(String.format("%n  More records available, use: --page-token %s", nextPageToken));
        }
        out.println("");
    }
}
