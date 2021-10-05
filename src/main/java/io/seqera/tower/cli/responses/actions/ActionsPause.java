package io.seqera.tower.cli.responses.actions;

import io.seqera.tower.cli.responses.Response;

public class ActionsPause extends Response {

    public final String actionName;
    public final String workspaceRef;

    public ActionsPause(String actionName, String workspaceRef) {
        this.actionName = actionName;
        this.workspaceRef = workspaceRef;
    }

    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline action '%s' paused at %s workspace|@%n", actionName, workspaceRef));
    }

}
