package io.seqera.tower.cli.responses;

public class CredentialsUpdated extends Response {

    public final String provider;
    public final String name;
    public final String workspaceRef;

    public CredentialsUpdated(String provider, String name, String workspaceRef) {
        this.provider = provider;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow %S credentials '%s' updated at %s workspace|@%n", provider, name, workspaceRef));
    }
}
