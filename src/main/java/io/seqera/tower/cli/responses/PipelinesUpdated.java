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
        return String.format("Pipeline '(%s) %s' updated at %s workspace", pipeline.getRepository(), pipeline.getName(), workspaceRef);
    }
}
