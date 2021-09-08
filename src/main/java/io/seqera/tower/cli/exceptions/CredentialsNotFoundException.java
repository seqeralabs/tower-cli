package io.seqera.tower.cli.exceptions;

public class CredentialsNotFoundException extends TowerException {

    public CredentialsNotFoundException(String name, String workspaceRef) {
        super(String.format("Credentials '%s' not found at %s workspace", name, workspaceRef));
    }
}
