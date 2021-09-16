package io.seqera.tower.cli.exceptions;

public class NoComputeEnvironmentException extends TowerException {
    public NoComputeEnvironmentException(String workspaceRef) {
        super(String.format("No available compute environment defined at %s workspace", workspaceRef));
    }
}
