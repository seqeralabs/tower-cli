package io.seqera.tower.cli.responses;

public class CredentialsUpdated extends Response {

    private String provider;
    private String name;
    private String workspaceRef;

    public CredentialsUpdated(String provider, String name, String workspaceRef) {
        this.provider = provider;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return String.format("%S credentials '%s' updated at %s workspace", provider, name, workspaceRef);
    }
}
