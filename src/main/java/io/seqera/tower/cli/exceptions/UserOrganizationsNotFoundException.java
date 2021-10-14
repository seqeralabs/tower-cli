package io.seqera.tower.cli.exceptions;

public class UserOrganizationsNotFoundException extends TowerException {

    public UserOrganizationsNotFoundException(String userName) {
        super(String.format("Organizations not found for user '%s'", userName));
    }
}
