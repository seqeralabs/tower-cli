package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.OrganizationDbDto;

public class OrganizationsCreated extends Response {

    public final OrganizationDbDto organization;

    public OrganizationsCreated(OrganizationDbDto organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Organization '%s' with ID '%d' was created|@%n", organization.getName(), organization.getOrgId()));
    }
}
