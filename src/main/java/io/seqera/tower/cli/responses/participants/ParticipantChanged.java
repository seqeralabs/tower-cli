package io.seqera.tower.cli.responses.participants;

import io.seqera.tower.cli.responses.Response;

public class ParticipantChanged extends Response {

    final public String workspaceName;
    final public String name;
    final public String role;

    public ParticipantChanged(String workspaceName, String name, String role) {
        this.workspaceName = workspaceName;
        this.name = name;
        this.role = role;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Participant '%s' has now role '%s' for workspace '%s'|@%n", name, role, workspaceName));
    }
}
