package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;

public class MembersLeave extends Response {

    public final String orgName;

    public MembersLeave(String orgName) {
        this.orgName = orgName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow You have been removed from organization '%s'|@%n", orgName));
    }
}
