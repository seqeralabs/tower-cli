package io.seqera.tower.cli.responses;

import io.seqera.tower.model.ComputeEnv;

public class ComputeEnvView extends Response {

    private String id;
    private ComputeEnv computeEnv;
    private String workspaceRef;

    public ComputeEnvView(String id, String workspaceRef, ComputeEnv computeEnv) {
        this.id = id;
        this.computeEnv = computeEnv;
    }

    @Override
    public Object getBody() {
        return computeEnv;
    }

    @Override
    public String toString() {
        String body = "";
        return String.format("Compute environment '%s' at %s workspace.%n%s", id, workspaceRef, body);
    }
}
