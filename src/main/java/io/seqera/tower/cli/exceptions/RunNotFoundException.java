package io.seqera.tower.cli.exceptions;

public class RunNotFoundException extends TowerException {
    public RunNotFoundException(String name, String workspaceRef) {
        super(String.format("Pipeline's run '%s' not found at %s workspace", name, workspaceRef));
    }
}
