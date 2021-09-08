package io.seqera.tower.cli.responses;

public class PipelinesCreated extends Response {

    private String workspaceRef;
    private String pipelineName;

    public PipelinesCreated(String workspaceRef, String pipelineName) {
        this.workspaceRef = workspaceRef;
        this.pipelineName = pipelineName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow New pipeline '%s' added at %s workspace|@%n", pipelineName, workspaceRef));
    }
}
