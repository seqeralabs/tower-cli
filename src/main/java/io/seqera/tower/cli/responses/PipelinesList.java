package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.PipelineDbDto;

import java.io.PrintWriter;
import java.util.List;

public class PipelinesList extends Response {

    private String workspaceRef;
    private final List<PipelineDbDto> pipelines;

    public PipelinesList(String workspaceRef, List<PipelineDbDto> pipelines) {
        this.workspaceRef = workspaceRef;
        this.pipelines = pipelines;
    }

    @Override
    public void toString(PrintWriter out) {

        out.println(ansi(String.format("%n  @|bold Pipelines at %s workspace:|@%n", workspaceRef)));

        if (pipelines.isEmpty()) {
            out.println(ansi("    @|yellow No pipelines found|@"));
            return;
        }

        TableList table = new TableList(out, 3, "ID", "Repository", "Description").sortBy(0).withUnicode(false);
        table.setPrefix("    ");
        pipelines.forEach(pipe -> table.addRow(pipe.getName(), pipe.getRepository(), pipe.getDescription()));
        table.print();
        out.println("");
    }
}
