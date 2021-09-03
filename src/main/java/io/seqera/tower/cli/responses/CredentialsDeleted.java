package io.seqera.tower.cli.responses;

public class CredentialsDeleted extends Response {

    private String id;
    private String workspaceRef;

    public CredentialsDeleted(String id, String workspaceRef) {
        this.id = id;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Credentials '%s' deleted at %s workspace|@%n", id, workspaceRef));
    }
}
