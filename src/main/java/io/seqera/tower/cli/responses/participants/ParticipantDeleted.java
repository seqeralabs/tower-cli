package io.seqera.tower.cli.responses.participants;

import io.seqera.tower.cli.responses.Response;

public class ParticipantDeleted extends Response {

    final public String name;
    final public String workspaceName;

    public ParticipantDeleted(String name, String workspaceName) {
        this.name = name;
        this.workspaceName = workspaceName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Participant '%s' was removed from '%s' workspace|@%n", name, workspaceName));
    }

}
