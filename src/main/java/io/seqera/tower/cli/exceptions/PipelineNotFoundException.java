package io.seqera.tower.cli.exceptions;

public class PipelineNotFoundException extends TowerException {

    public PipelineNotFoundException(String pipelineName, String workspaceRef) {
        super(String.format("Unknown pipeline '%s' at %s workspace", pipelineName, workspaceRef));
    }

    public PipelineNotFoundException(Long pipelineId, String workspaceRef) {
        super(String.format("Unknown pipeline '%s' at %s workspace", pipelineId, workspaceRef));
    }
}
