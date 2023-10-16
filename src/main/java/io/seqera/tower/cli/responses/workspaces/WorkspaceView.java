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

package io.seqera.tower.cli.responses.workspaces;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Workspace;

import java.io.PrintWriter;

public class WorkspaceView extends Response {

    public final Workspace workspace;

    public WorkspaceView(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Details for workspace '%s'|@%n", workspace.getFullName())));

        TableList table = new TableList(out, 2);
        table.setPrefix(" ");
        table.addRow("ID", workspace.getId().toString());
        table.addRow("Name", workspace.getName());
        table.addRow("Full Name", workspace.getFullName());
        table.addRow("Description", workspace.getDescription());
        table.addRow("Visibility", workspace.getVisibility().toString());
        table.addRow("Date Created", FormatHelper.formatTime(workspace.getDateCreated()));
        table.addRow("Last Updated", FormatHelper.formatTime(workspace.getLastUpdated()));
        table.print();
        out.println("");
    }
}
