package io.seqera.tower.cli.responses.actions;

import java.io.PrintWriter;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Action;

public class ActionsView extends Response {

    final public Action action;

    public ActionsView(Action action) {
        this.action = action;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Details for action '%s'|@%n", action.getName())));

        TableList table = new TableList(out, 2);
        table.addRow("ID", action.getId());
        table.addRow("Name", action.getName());
        table.addRow("Pipeline URL", action.getLaunch().getPipeline());
        table.addRow("Source", action.getSource().toString());
        table.addRow("Status", action.getStatus().toString());
        table.addRow("Date created", formatTime(action.getDateCreated()));
        table.addRow("Last event", formatTime(action.getLastSeen()));
        out.println("");
    }
}
