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

package io.seqera.tower.cli.responses.datastudios;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioStatusInfo;

import java.io.PrintWriter;

import static io.seqera.tower.cli.utils.FormatHelper.formatDataStudioStatus;
import static io.seqera.tower.cli.utils.FormatHelper.formatTime;

public class DataStudiosView extends Response {

    public final DataStudioDto dataStudio;
    public final String workspaceRef;

    public DataStudiosView(DataStudioDto dataStudioDto, String workspaceRef) {
        this.dataStudio = dataStudioDto;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public void toString(PrintWriter out){
        out.println(ansi(String.format("%n  @|bold DataStudio at workspace '%s'|@%n", workspaceRef)));

        DataStudioStatusInfo statusInfo = dataStudio.getStatusInfo();
        TableList table = new TableList(out, 2);
        table.setPrefix(" ");
        table.addRow("SessionID", dataStudio.getSessionId());
        table.addRow("Name", dataStudio.getName());
        table.addRow("Description", dataStudio.getDescription());
        table.addRow("Template", dataStudio.getTemplate() == null ? "NA" : dataStudio.getTemplate().getRepository());
        table.addRow("Status", formatDataStudioStatus(statusInfo == null ? null : statusInfo.getStatus()));
        table.addRow("Status Last Update", statusInfo == null ? "NA" : formatTime(statusInfo.getLastUpdate()));
        table.addRow("Studio Created", formatTime(dataStudio.getDateCreated()));
        table.addRow("Studio Last Updated", formatTime(dataStudio.getLastUpdated()));
        table.print();

    }
}
