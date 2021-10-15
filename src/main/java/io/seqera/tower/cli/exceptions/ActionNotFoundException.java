package io.seqera.tower.cli.exceptions;

public class ActionNotFoundException extends TowerException {

    public ActionNotFoundException(String workspaceName) {
        super(String.format("No actions found for workspace '%s'", workspaceName));
    }

    public ActionNotFoundException(String actionName, String workspaceName) {
        super(String.format("No action '%s' found for workspace '%s'", actionName, workspaceName));
    }
}
