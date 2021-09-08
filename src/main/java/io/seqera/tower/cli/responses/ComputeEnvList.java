package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class ComputeEnvList extends Response {

    private String workspaceRef;
    private List<ListComputeEnvsResponseEntry> computeEnvs;

    public ComputeEnvList(String workspaceRef, List<ListComputeEnvsResponseEntry> computeEnvs) {
        this.workspaceRef = workspaceRef;
        this.computeEnvs = computeEnvs;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Compute environments at %s workspace:|@%n", workspaceRef)));

        if (computeEnvs.isEmpty()) {
            out.println(ansi("    @|yellow No compute environment found|@"));
            return;
        }

        TableList table = new TableList(out, 5, "ID", "Status", "Platform", "Name", "Last activity").sortBy(0).withUnicode(false);
        table.setPrefix("    ");
        computeEnvs.forEach(ce -> table.addRow(ce.getId(), ce.getStatus().getValue(), ce.getPlatform(), ce.getName(), formatTime(ce.getLastUsed())));
        table.print();
        out.println("");
    }
}
