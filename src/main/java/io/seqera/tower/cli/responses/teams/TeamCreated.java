package io.seqera.tower.cli.responses.teams;

import io.seqera.tower.cli.responses.Response;

public class TeamCreated extends Response {

    public final String organizationName;
    public final String teamName;

    public TeamCreated(String organizationName, String teamName) {
        this.organizationName = organizationName;
        this.teamName = teamName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow A '%s' team created for '%s' organization|@%n", teamName, organizationName));
    }

}
