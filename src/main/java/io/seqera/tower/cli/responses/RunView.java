package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;

import java.io.PrintWriter;
import java.util.Map;

public class RunView extends Response {

    public final String id;
    public final Workflow workflow;
    public final WorkflowLoad workflowLoad;
    public final ComputeEnv computeEnv;
    public final String workspaceRef;

    public RunView(String id, String workspaceRef, Workflow workflow, WorkflowLoad workflowLoad, ComputeEnv computeEnv) {
        this.id = id;
        this.workspaceRef = workspaceRef;
        this.workflow = workflow;
        this.workflowLoad = workflowLoad;
        this.computeEnv = computeEnv;
    }

    @Override
    public Object getJSON() {
        return workflow;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Run at %s workspace:|@%n", workspaceRef)));

        out.println(ansi(String.format("%n    @|bold General|@")));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", workflow.getId());
        table.addRow("Run name", workflow.getRunName());
        table.addRow("Starting date", workflow.getStart() != null ? formatTime(workflow.getStart()) : "No date reported");
        table.addRow("Commit ID", workflow.getCommitId() != null ? workflow.getCommitId() : "No commit ID reported");
        table.addRow("Session ID", workflow.getSessionId());
        table.addRow("Username", workflow.getUserName());
        table.addRow("Workdir", workflow.getWorkDir());
        table.addRow("Container", String.join(", ", workflow.getContainer()));
        table.addRow("Executors", workflowLoad != null && workflowLoad.getExecutors() != null ? String.join(", ", workflowLoad.getExecutors()) : "No executors were reported");
        table.addRow("Compute Environment", computeEnv.getName());
        table.addRow("Nextflow Version", workflow.getNextflow() != null ? workflow.getNextflow().getVersion() : "No Nextflow version reported");
        table.print();

        if (workflow.getManifest() != null) {
            int manifestCount = 0;

            TableList manifestTable = new TableList(out, 2);
            manifestTable.setPrefix("    ");
            if (workflow.getManifest().getNextflowVersion() != null) {
                manifestTable.addRow("NF version", workflow.getManifest().getNextflowVersion());
                ++manifestCount;
            }
            if (workflow.getManifest().getDefaultBranch() != null) {
                manifestTable.addRow("Default branch", workflow.getManifest().getDefaultBranch());
                ++manifestCount;
            }
            if (workflow.getManifest().getVersion() != null) {
                manifestTable.addRow("Version", workflow.getManifest().getVersion());
                ++manifestCount;
            }
            if (workflow.getManifest().getHomePage() != null) {
                manifestTable.addRow("Homepage", workflow.getManifest().getHomePage());
                ++manifestCount;
            }
            if (workflow.getManifest().getGitmodules() != null) {
                manifestTable.addRow("Git modules", workflow.getManifest().getGitmodules());
                ++manifestCount;
            }
            if (workflow.getManifest().getDescription() != null) {
                manifestTable.addRow("Description", workflow.getManifest().getDescription());
                ++manifestCount;
            }
            if (workflow.getManifest().getName() != null) {
                manifestTable.addRow("Name", workflow.getManifest().getName());
                ++manifestCount;
            }
            if (workflow.getManifest().getMainScript() != null) {
                manifestTable.addRow("Main script", workflow.getManifest().getMainScript());
                ++manifestCount;
            }
            if (workflow.getManifest().getAuthor() != null) {
                manifestTable.addRow("Author", workflow.getManifest().getAuthor());
                ++manifestCount;
            }

            if (manifestCount > 0) {
                out.println(ansi(String.format("%n%n    @|bold Manifest|@")));
                manifestTable.print();
            }
        }

        if (workflow.getStats() != null) {
            out.println(ansi(String.format("%n%n    @|bold Status|@")));

            TableList statsTable = new TableList(out, 2);
            statsTable.setPrefix("    ");
            statsTable.addRow("Pending", workflowLoad.getPending().toString());
            statsTable.addRow("Submitted", workflowLoad.getSubmitted().toString());
            statsTable.addRow("Running", workflowLoad.getRunning().toString());
            statsTable.addRow("Cached", workflowLoad.getCached().toString());
            statsTable.addRow("Succeeded", workflowLoad.getSucceeded().toString());
            statsTable.addRow("getFailed", workflowLoad.getFailed().toString());
            statsTable.print();
        }


        if (workflow.getParams() != null || !workflow.getParams().isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Parameters|@")));
            TableList paramsTable = new TableList(out, 2);
            paramsTable.setPrefix("    ");
            workflow.getParams().forEach((key, value) -> paramsTable.addRow(key, value.toString()));
        }

        if (workflow.getConfigText() != null && !workflow.getConfigText().isBlank()) {
            out.println(ansi(String.format("%n    @|bold Configuration|@")));
            out.println(workflow.getConfigText());
        }
    }
}
