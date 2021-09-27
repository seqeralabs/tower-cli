package io.seqera.tower.cli.exceptions;

public class WorkflowProgressNotFoundException extends TowerException {
    public WorkflowProgressNotFoundException(String id, String workspaceRef) {
        super(String.format("Workflow progress '%s' not found at %s workspace", id, workspaceRef));
    }
}
