package io.seqera.tower.cli.responses.organizations;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Visibility;

public class OrganizationsUpdated extends Response {

    public final String organizationName;

    public OrganizationsUpdated(String organizationName) {
        this.organizationName = organizationName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Organization '%s' was updated|@%n", organizationName));
    }

}
