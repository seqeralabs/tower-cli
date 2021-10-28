package io.seqera.tower.cli.responses.pipelines;

import io.seqera.tower.cli.responses.Response;

public class PipelinesDeleted extends Response {

    public final String id;
    public final String workspaceRef;

    public PipelinesDeleted(String id, String workspaceRef) {
        this.id = id;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline '%s' deleted at %s workspace|@%n", id, workspaceRef));
    }
}
