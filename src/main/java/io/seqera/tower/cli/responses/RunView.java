package io.seqera.tower.cli.responses;

import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.Workflow;

import java.io.PrintWriter;

public class RunView extends Response {

    public final String id;
    public final Workflow workflow;
    public final String workspaceRef;

    public RunView(String id, String workspaceRef, Workflow workflow) {
        this.id = id;
        this.workflow = workflow;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public Object getJSON() {
        return workflow;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Run at %s workspace:|@%n", workspaceRef)));

        out.println(ansi(String.format("%n    @|bold General pipeline run|@")));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("Status", workflow.getStatus() != null ? workflow.getStatus().toString() : "No status reported");
        table.addRow("owner", workflow.getOwnerId() != null ? workflow.getOwnerId().toString() : "No owner reported");
        table.addRow("ID", workflow.getId());
        table.addRow("Submission date", workflow.getSubmit().toString());
        table.addRow("Starting date", workflow.getStart() != null ? workflow.getStart().toString() : "No starting date reported");
        table.addRow("Completed date", workflow.getComplete() != null ? workflow.getComplete().toString() : "Not completed date reported");
        table.addRow("Created date", workflow.getDateCreated().toString());
        table.addRow("Updated date", workflow.getLastUpdated().toString());
        table.addRow("Run name", workflow.getRunName());
        table.addRow("Session ID", workflow.getSessionId());
        table.addRow("Profile", workflow.getProfile());
        table.addRow("Workdir", workflow.getWorkDir());
        table.addRow("Commit ID", workflow.getCommitId());
        table.addRow("Username", workflow.getUserName());
        table.addRow("Script ID", workflow.getScriptId());
        table.addRow("Revision", workflow.getRevision());
        table.addRow("Command line", workflow.getCommandLine());
        table.addRow("Project name", workflow.getProjectName());
        table.addRow("Script name", workflow.getScriptName());
        table.addRow("Launch ID", workflow.getLaunchId());
        table.addRow("Config files", workflow.getConfigFiles() != null ? workflow.getConfigFiles().toString() : "No related config files found");
        table.addRow("Params", workflow.getParams() != null ? workflow.getParams().toString() : "No params configured");
        table.addRow("Config text", workflow.getConfigText() != null ? workflow.getConfigText().replace("\n", "\\n") : "No config text available");
        table.addRow("Error message", workflow.getErrorMessage() != null ? workflow.getErrorMessage() : "No errors reported");
        table.addRow("Error report", workflow.getErrorReport() != null ? workflow.getErrorMessage() : "No error report available");
        table.addRow("Deleted", workflow.getDeleted() != null ? workflow.getDeleted().toString() : "false");
        table.addRow("Project directory", workflow.getProjectDir());
        table.addRow("Home directory", workflow.getHomeDir());
        table.addRow("Container", workflow.getContainer());
        table.addRow("Repository", workflow.getRepository());
        table.addRow("Container engine", workflow.getContainerEngine() != null ? workflow.getContainerEngine() : "Container engine no specified");
        table.addRow("Script file", workflow.getScriptFile());
        table.addRow("Launch directory", workflow.getLaunchDir());
        table.addRow("Duration", workflow.getDuration() != null ? workflow.getDuration().toString() : "Not reported");
        table.addRow("Exit status", workflow.getExitStatus() != null ? workflow.getExitStatus().toString() : "No exit status reported");
        table.addRow("Resumed", workflow.getResume() != null ? workflow.getResume().toString() : "false");
        table.addRow("Success", workflow.getSuccess() != null ? workflow.getSuccess().toString() : "Not reported");
        table.addRow("Log file", workflow.getLogFile() != null ? workflow.getLogFile() : "No log file specified");
        table.addRow("Output file", workflow.getOutFile() != null ? workflow.getOutFile() : "No output file specified");
        table.addRow("Operation ID", workflow.getOperationId() != null ? workflow.getOperationId() : "No operation ID specified");
        table.print();


        if (workflow.getManifest() != null) {
            out.println(ansi(String.format("%n%n    @|bold General pipeline run manifest|@")));

            TableList manifestTable = new TableList(out, 2);
            manifestTable.setPrefix("    ");
            manifestTable.addRow("NF version", workflow.getManifest().getNextflowVersion() != null ? workflow.getManifest().getNextflowVersion() : "No Nextflow version reported");
            manifestTable.addRow("Default branch", workflow.getManifest().getDefaultBranch());
            manifestTable.addRow("Version", workflow.getManifest().getVersion() != null ? workflow.getManifest().getVersion() : "No version reported");
            manifestTable.addRow("Homepage", workflow.getManifest().getHomePage() != null ? workflow.getManifest().getHomePage() : "No homepage reported");
            manifestTable.addRow("Git modules", workflow.getManifest().getGitmodules() != null ? workflow.getManifest().getGitmodules() : "No git modules reported");
            manifestTable.addRow("Description", workflow.getManifest().getDescription() != null ? workflow.getManifest().getDescription() : "No description specified");
            manifestTable.addRow("Name", workflow.getManifest().getName() != null ? workflow.getManifest().getName() : "No name specified");
            manifestTable.addRow("Main script", workflow.getManifest().getMainScript());
            manifestTable.addRow("Author", workflow.getManifest().getAuthor() != null ? workflow.getManifest().getAuthor() : "Unknown author");
            manifestTable.print();
        }

        if (workflow.getNextflow() != null) {
            out.println(ansi(String.format("%n%n    @|bold Pipeline run Nextflow version|@")));

            TableList nfTable = new TableList(out, 2);
            nfTable.setPrefix("    ");
            nfTable.addRow("Version number", workflow.getNextflow().getVersion());
            nfTable.addRow("Build number", workflow.getNextflow().getBuild());
            nfTable.addRow("Timestamp", workflow.getNextflow().getTimestamp().toString());
            nfTable.print();
        }

        if (workflow.getStats() != null) {
            out.println(ansi(String.format("%n%n    @|bold Pipeline run stats|@")));

            TableList statsTable = new TableList(out, 2);
            statsTable.setPrefix("    ");
            statsTable.addRow("Compute time", workflow.getStats().getComputeTimeFmt());
            statsTable.addRow("Cached count", workflow.getStats().getCachedCount().toString());
            statsTable.addRow("Failed count", workflow.getStats().getFailedCount().toString());
            statsTable.addRow("Ignored count", workflow.getStats().getIgnoredCount().toString());
            statsTable.addRow("Success count", workflow.getStats().getSucceedCount().toString());
            statsTable.addRow("Cached count FMT", workflow.getStats().getCachedCount().toString());
            statsTable.addRow("Failed count FMT", workflow.getStats().getFailedCountFmt());
            statsTable.addRow("Ignored count FMT", workflow.getStats().getIgnoredCountFmt());
            statsTable.addRow("Success count FMT", workflow.getStats().getSucceedCountFmt());
            statsTable.addRow("Cached Pct.", workflow.getStats().getCachedPct().toString());
            statsTable.addRow("Failed Pct.", workflow.getStats().getFailedPct().toString());
            statsTable.addRow("Ignored Pct.", workflow.getStats().getIgnoredPct().toString());
            statsTable.addRow("Success Pct.", workflow.getStats().getSucceedPct().toString());
            statsTable.addRow("Cache duration", workflow.getStats().getCachedDuration().toString());
            statsTable.addRow("Failed duration", workflow.getStats().getFailedDuration().toString());
            statsTable.addRow("Success duration", workflow.getStats().getSucceedDuration().toString());
            statsTable.print();
        }

//        out.println(ansi(String.format("%n%n    @|bold Pipeline run workflow progress|@")));
//
//        TableList wfProgressTable = new TableList(out, 2);
//        wfProgressTable.setPrefix("    ");
//        wfProgressTable.print();
//
//        out.println(ansi(String.format("%n%n    @|bold Pipeline run process progress|@")));
//
//        TableList processProgressTable = new TableList(out, 2);
//        processProgressTable.setPrefix("    ");
//        processProgressTable.print();
    }
}
