package io.seqera.tower.cli.responses.actions;

import io.seqera.tower.cli.responses.Response;

public class ActionUpdate extends Response {

    public final String actionName;
    public final String workspaceRef;
    public final String actionId;

    public ActionUpdate(String actionName, String workspaceRef, String actionId) {
        this.actionName = actionName;
        this.workspaceRef = workspaceRef;
        this.actionId = actionId;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline action '%s' updated at %s workspace with id '%s'|@%n", actionName, workspaceRef, actionId));
    }
}
