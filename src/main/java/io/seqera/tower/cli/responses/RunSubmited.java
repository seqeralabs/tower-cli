package io.seqera.tower.cli.responses;

public class RunSubmited extends Response {

    private String workflowId;

    private String workflowUrl;

    private String workspaceRef;

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
