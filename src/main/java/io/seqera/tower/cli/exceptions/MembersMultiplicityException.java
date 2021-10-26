package io.seqera.tower.cli.exceptions;

public class MembersMultiplicityException extends TowerException {

    public MembersMultiplicityException(String user, Long orgId) {
        super(String.format("Multiple Members were found for the '%s' keyword %d organization", user, orgId));
    }
}
