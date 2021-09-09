package io.seqera.tower.cli.exceptions;

public class WorkspaceNotFoundException extends TowerException {

    public WorkspaceNotFoundException(Long workspaceId) {
        super(String.format("Workspace '%d' not found", workspaceId));
    }

    public WorkspaceNotFoundException(String workspaceName, String orgName) {
        super(String.format("Workspace '%s' at organization '%s' not found", workspaceName, orgName));
    }
}
