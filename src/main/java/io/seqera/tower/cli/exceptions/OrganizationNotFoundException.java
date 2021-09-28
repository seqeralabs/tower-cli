package io.seqera.tower.cli.exceptions;

public class OrganizationNotFoundException extends TowerException {

    public OrganizationNotFoundException(Long organizationId) {
        super(String.format("Organization '%d' not found", organizationId));
    }

    public OrganizationNotFoundException(String organizationName) {
        super(String.format("Organization '%s' not found", organizationName));
    }
}
