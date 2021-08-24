package io.seqera.tower.cli.responses;

public class CredentialsCreated implements Response {

    private String provider;
    private String name;
    private String workspaceRef;

    public CredentialsCreated(String provider, String name, String workspaceRef) {
        this.provider = provider;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return String.format("New %S credentials '%s' added at %s workspace", provider, name, workspaceRef);
    }
}
