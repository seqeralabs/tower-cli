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

package io.seqera.tower.cli.responses.workspaces;

import io.seqera.tower.cli.responses.Response;
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
        table.addRow("Date Created", formatTime(workspace.getDateCreated()));
        table.addRow("Last Updated", formatTime(workspace.getLastUpdated()));
        table.print();
        out.println("");
    }
}
