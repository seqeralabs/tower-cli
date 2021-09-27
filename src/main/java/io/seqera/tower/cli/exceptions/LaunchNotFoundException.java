package io.seqera.tower.cli.exceptions;

public class LaunchNotFoundException extends TowerException {
    public LaunchNotFoundException(String name, String workspaceRef) {
        super(String.format("Workflow launch '%s' not found at %s workspace", name, workspaceRef));
    }
}
