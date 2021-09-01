package io.seqera.tower.cli.responses;

import io.seqera.tower.model.PipelineDbDto;

public class PipelinesCreated extends Response {

    private String workspaceRef;
    private PipelineDbDto pipeline;

    public PipelinesCreated(String workspaceRef, PipelineDbDto pipeline) {
        this.workspaceRef = workspaceRef;
        this.pipeline = pipeline;
    }

    @Override
    public String toString() {
        return String.format("New pipeline '(%s) %s' added at %s workspace", pipeline.getRepository(), pipeline.getName(), workspaceRef);
    }
}
