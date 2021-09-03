package io.seqera.tower.cli.responses;

public class ComputeEnvCreated extends Response {

    private String platform;
    private String name;
    private String workspaceRef;

    public ComputeEnvCreated(String platform, String name, String workspaceRef) {
        this.platform = platform;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow New %S compute environment '%s' added at %s workspace|@%n", platform, name, workspaceRef));
    }
}
