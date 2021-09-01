package io.seqera.tower.cli.responses;

public class PipelinesDeleted extends Response {

    private String id;
    private String workspaceRef;

    public PipelinesDeleted(String id, String workspaceRef) {
        this.id = id;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return String.format("Pipeline '%s' deleted at %s workspace", id, workspaceRef);
    }
}
