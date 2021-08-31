package io.seqera.tower.cli.responses;

public class CredentialsCreated extends Response {

    private String id;
    private String provider;
    private String name;
    private String workspaceRef;

    public CredentialsCreated(String provider, String id, String name, String workspaceRef) {
        this.provider = provider;
        this.id = id;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return String.format("New %S credentials '%s (%s)' added at %s workspace", provider, name, id, workspaceRef);
    }
}