package io.seqera.tower.cli.responses.participants;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;

public class ParticipantAdded extends Response {

    final public ParticipantDbDto participant;
    final public String workspaceName;

    public ParticipantAdded(ParticipantDbDto participant, String workspaceName) {
        this.participant = participant;
        this.workspaceName = workspaceName;
    }

    @Override
    public String toString() {

        if (participant.getType() == ParticipantType.TEAM) {
            return ansi(String.format("%n  @|yellow Team '%s' was added as participant to '%s' workspace with role '%s'|@%n", participant.getTeamName(), workspaceName, participant.getWspRole()));
        }

        return ansi(String.format("%n  @|yellow User '%s' was added as participant to '%s' workspace with role '%s'|@%n", participant.getUserName(), workspaceName, participant.getWspRole()));
    }

}
