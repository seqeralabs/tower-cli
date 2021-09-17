package io.seqera.tower.cli.responses;

public class RunCreated extends Response {

    public final String workflowId;
    public final String workspaceRef;

    public RunCreated(String workflowId, String workspaceRef) {
        this.workflowId = workflowId;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow New pipeline's run '%s' added at %s workspace|@%n", workflowId, workspaceRef));
    }
}
