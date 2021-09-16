package io.seqera.tower.cli.responses;

public class RunSubmited extends Response {

    public final String workflowId;

    public final String workflowUrl;

    public final String workspaceRef;

    public RunSubmited(String workflowId, String workflowUrl, String workspaceRef) {
        this.workflowId = workflowId;
        this.workflowUrl = workflowUrl;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Workflow %s submitted at %s workspace.|@%n%n    @|bold %s|@%n", workflowId, workspaceRef, workflowUrl));
    }

}
