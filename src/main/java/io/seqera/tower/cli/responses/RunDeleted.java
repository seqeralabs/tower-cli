package io.seqera.tower.cli.responses;

public class RunDeleted extends Response {

    public final String id;
    public final String workspaceRef;

    public RunDeleted(String id, String workspaceRef) {
        this.id = id;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline's run '%s' deleted at %s workspace|@%n", id, workspaceRef));
    }
}
