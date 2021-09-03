package io.seqera.tower.cli.responses;

import io.seqera.tower.model.PipelineDbDto;

public class PipelinesUpdated extends Response {

    private String workspaceRef;
    private PipelineDbDto pipeline;

    public PipelinesUpdated(String workspaceRef, PipelineDbDto pipeline) {
        this.workspaceRef = workspaceRef;
        this.pipeline = pipeline;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline '%s' updated at %s workspace|@%n", pipeline.getName(), workspaceRef));
    }
}
