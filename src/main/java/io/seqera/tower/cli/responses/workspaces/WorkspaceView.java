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
        out.println(ansi(String.format("%n  @|bold Details for workspace '%s@|%n'", workspace.getFullName())));

        TableList table = new TableList(out, 7, "ID", "Name", "Full Name", "Description", "Visibility", "Date Created", "Last Updated").sortBy(0);
        table.setPrefix(" ");
        table.addRow(workspace.getId().toString(), workspace.getName(), workspace.getFullName(), workspace.getDescription(), workspace.getVisibility().toString(), formatTime(workspace.getDateCreated()), formatTime(workspace.getLastUpdated()));

        table.print();
        out.println("");
    }
}
