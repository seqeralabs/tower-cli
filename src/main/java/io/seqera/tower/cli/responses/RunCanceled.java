package io.seqera.tower.cli.responses;

public class RunCanceled extends Response{

    public final String id;
    public final String workspaceRef;

    public RunCanceled(String id, String workspaceRef) {
        this.id = id;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline's run '%s' canceled at %s workspace|@%n", id, workspaceRef));
    }

}
