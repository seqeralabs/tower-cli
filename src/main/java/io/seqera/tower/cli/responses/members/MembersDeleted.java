package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;

public class MembersDeleted extends Response {

    public final String user;
    public final String organizationName;

    public MembersDeleted(String user, String organizationName) {
        this.user = user;
        this.organizationName = organizationName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' deleted from organization '%s'|@%n", user, organizationName));
    }
}
