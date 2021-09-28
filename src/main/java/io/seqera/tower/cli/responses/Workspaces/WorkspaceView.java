package io.seqera.tower.cli.responses.Workspaces;

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
        out.println(ansi(String.format("%n  @|bold Details for workspace '%s'", workspace.getFullName())));

        TableList table = new TableList(out, 3, "Workspace ID", "Workspace Name", "Workspace Full Name").sortBy(0);
        table.addRow(workspace.getId().toString(), workspace.getName(), workspace.getFullName());

        table.print();
        out.println("");
    }
}
