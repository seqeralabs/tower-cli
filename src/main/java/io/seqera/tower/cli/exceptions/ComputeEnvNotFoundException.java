package io.seqera.tower.cli.exceptions;

public class ComputeEnvNotFoundException extends TowerException {
    public ComputeEnvNotFoundException(String name, String workspaceRef) {
        super(String.format("Compute environment '%s' not found at %s workspace", name, workspaceRef));
    }

    public ComputeEnvNotFoundException(String name, long workspaceId) {
        super(String.format("Compute environment '%s' not found at %d workspace", name, workspaceId));
    }
}
