package io.seqera.tower.cli.responses.teams.members;

import io.seqera.tower.cli.responses.Response;

public class TeamMemberDeleted extends Response {

    Long teamId;
    String username;

    public TeamMemberDeleted(Long teamId, String username) {
        this.teamId = teamId;
        this.username = username;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Team member '%s' deleted at '%d' team|@%n", username, teamId));
    }

}
