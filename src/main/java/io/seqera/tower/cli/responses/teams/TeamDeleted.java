package io.seqera.tower.cli.responses.teams;

import io.seqera.tower.cli.responses.Response;

public class TeamDeleted extends Response {

    public final String organizationName;
    public final String teamId;

    public TeamDeleted(String organizationName, String teamId) {
        this.organizationName = organizationName;
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Team '%s' deleted for %s organization|@%n", teamId, organizationName));
    }

}
