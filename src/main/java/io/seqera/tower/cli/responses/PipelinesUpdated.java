package io.seqera.tower.cli.responses;

public class PipelinesUpdated extends Response {

    private String workspaceRef;
    private String pipelineName;

    public PipelinesUpdated(String workspaceRef, String pipelineName) {
        this.workspaceRef = workspaceRef;
        this.pipelineName = pipelineName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline '%s' updated at %s workspace|@%n", pipelineName, workspaceRef));
    }
}
