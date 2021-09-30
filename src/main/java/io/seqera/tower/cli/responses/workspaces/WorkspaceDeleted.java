package io.seqera.tower.cli.responses.workspaces;

import io.seqera.tower.cli.responses.Response;

public class WorkspaceDeleted extends Response {

    public final String workspaceRef;
    public final String organizationRef;

    public WorkspaceDeleted(String workspaceRef, String organizationRef) {
        this.workspaceRef = workspaceRef;
        this.organizationRef = organizationRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Workspace '%s' deleted for %s organization|@%n", workspaceRef, organizationRef));
    }
}
