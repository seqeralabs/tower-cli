package io.seqera.tower.cli.exceptions;

public class ParticipantNotFoundException extends TowerException {

    public ParticipantNotFoundException(Long workspaceId, String participantName) {
        super(String.format("Participant '%s' not found in workspace '%d'", participantName, workspaceId));
    }
}
