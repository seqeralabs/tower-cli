package io.seqera.tower.cli.exceptions;

public class MembersNotFoundException extends TowerException {

    public MembersNotFoundException(String user, Long orgId) {
        super(String.format("Member '%s' not found in %d organization", user, orgId));
    }
}
