package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.responses.Response;

import javax.annotation.Nullable;

public class DeleteLabelsResponse extends Response {

    Long labelId;
    @Nullable
    Long workspaceId;

    public DeleteLabelsResponse(final Long labelId, final Long workspaceId) {
        this.labelId = labelId;
        this.workspaceId = workspaceId;
    }

    @Override
    public String toString() {
        return ansi(String.format(
                "%n  @|yellow Label '%d' deleted"
                + ( workspaceId != null ? " at '%d' workspace" : "" )
                + " |@%n",
        labelId, workspaceId));
    }

}
