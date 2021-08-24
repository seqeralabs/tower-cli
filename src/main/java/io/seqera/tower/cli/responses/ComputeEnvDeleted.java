package io.seqera.tower.cli.responses;

public class ComputeEnvDeleted extends Response {

    private String id;
    private String workspaceRef;

    public ComputeEnvDeleted(String id, String workspaceRef) {
        this.id = id;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return String.format("Compute environment '%s' deleted at %s workspace", id, workspaceRef);
    }
}
