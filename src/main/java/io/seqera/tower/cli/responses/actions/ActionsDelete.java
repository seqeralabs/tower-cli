package io.seqera.tower.cli.responses.actions;

import io.seqera.tower.cli.responses.Response;

public class ActionsDelete extends Response {

    public final String actionName;
    public final String workspaceRef;

    public ActionsDelete(String actionName, String workspaceRef) {
        this.actionName = actionName;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline action '%s' deleted at %s workspace|@%n", actionName, workspaceRef));
    }
}
