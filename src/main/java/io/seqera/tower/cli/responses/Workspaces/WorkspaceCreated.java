package io.seqera.tower.cli.responses.Workspaces;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Visibility;

public class WorkspaceCreated extends Response {

    public final String workspaceName;
    public final String organizationName;
    public final Visibility visibility;

    public WorkspaceCreated(String workspaceName, String organizationName, Visibility visibility) {
        this.workspaceName = workspaceName;
        this.organizationName = organizationName;
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow A '%s' workspace '%s' created for '%s' organization|@%n", visibility, workspaceName, organizationName));
    }

}
