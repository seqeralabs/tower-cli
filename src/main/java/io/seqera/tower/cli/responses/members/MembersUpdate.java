package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;

public class MembersUpdate extends Response {

    public final String user;
    public final String organizationName;
    public final String role;

    public MembersUpdate(String user, String organizationName, String role) {
        this.user = user;
        this.organizationName = organizationName;
        this.role = role;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' updated to role '%s' in organization '%s'|@%n", user, role, organizationName));
    }
}
