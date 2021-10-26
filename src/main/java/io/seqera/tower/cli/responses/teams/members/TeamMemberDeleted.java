package io.seqera.tower.cli.responses.teams.members;

import io.seqera.tower.cli.responses.Response;

public class TeamMemberDeleted extends Response {

    public final String teamName;
    public final String username;

    public TeamMemberDeleted(String teamName, String username) {
        this.teamName = teamName;
        this.username = username;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Team member '%s' deleted at '%s' team|@%n", username, teamName));
    }

}
