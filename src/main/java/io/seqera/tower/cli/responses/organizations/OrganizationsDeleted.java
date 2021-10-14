package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;

public class OrganizationsDeleted extends Response {

    public final String organizationRef;

    public OrganizationsDeleted(String organizationRef) {
        this.organizationRef = organizationRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Organization '%s' deleted|@%n", organizationRef));
    }
}
