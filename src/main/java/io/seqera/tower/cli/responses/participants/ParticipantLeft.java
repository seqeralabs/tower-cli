package io.seqera.tower.cli.responses.participants;

import io.seqera.tower.cli.responses.Response;

public class ParticipantLeft extends Response {

    final public String workspaceName;

    public ParticipantLeft(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow You have been removed as a participant from '%s' workspace|@%n", workspaceName));
    }

}
