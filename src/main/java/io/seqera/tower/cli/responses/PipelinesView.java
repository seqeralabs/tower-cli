package io.seqera.tower.cli.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.utils.ModelHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;

import java.io.PrintWriter;

public class PipelinesView extends Response {

    public final String workspaceRef;
    public final PipelineDbDto info;
    public final Launch launch;

    public PipelinesView(String workspaceRef, PipelineDbDto info, Launch launch) {
        this.workspaceRef = workspaceRef;
        this.info = info;
        this.launch = launch;
    }

    @Override
    public void toString(PrintWriter out) {
        String configJson = "";
        try {
            WorkflowLaunchRequest request = ModelHelper.createLaunchRequest(launch);
            configJson = new JSON().getContext(WorkflowLaunchRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        out.println(ansi(String.format("%n  @|bold Pipeline at %s workspace:|@%n", workspaceRef)));
        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", info.getPipelineId().toString());
        table.addRow("Name", info.getName());
        table.addRow("Description", info.getDescription());
        table.addRow("Repository", info.getRepository());
        table.addRow("Compute env.", launch.getComputeEnv().getName());
        table.print();

        out.println(String.format("%n  Configuration:%n%n%s%n", configJson.replaceAll("(?m)^", "     ")));
    }
}
