package io.seqera.tower.cli.responses;

public class CredentialsDeleted implements Response {

    private String id;
    private String workspaceRef;

    public CredentialsDeleted(String id, String workspaceRef) {
        this.id = id;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return String.format("Credentials '%s' deleted at %s workspace", id, workspaceRef);
    }
}
