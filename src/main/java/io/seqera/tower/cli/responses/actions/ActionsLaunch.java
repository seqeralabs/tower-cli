package io.seqera.tower.cli.responses.actions;

import io.seqera.tower.cli.responses.Response;

public class ActionsLaunch extends Response {

    public final String actionName;
    public final String workspaceRef;
    public final String workflowId;

    public ActionsLaunch(String actionName, String workspaceRef, String workflowId) {
        this.actionName = actionName;
        this.workspaceRef = workspaceRef;
        this.workflowId = workflowId;
    }

    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline action '%s' launched at %s workspace with run ID '%s'|@%n", actionName, workspaceRef, workflowId));
    }

}
