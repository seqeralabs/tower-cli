package io.seqera.tower.cli.responses.pipelines;

import io.seqera.tower.cli.responses.Response;

public class PipelinesUpdated extends Response {

    public final String workspaceRef;
    public final String pipelineName;

    public PipelinesUpdated(String workspaceRef, String pipelineName) {
        this.workspaceRef = workspaceRef;
        this.pipelineName = pipelineName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline '%s' updated at %s workspace|@%n", pipelineName, workspaceRef));
    }
}
