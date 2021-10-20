package io.seqera.tower.cli.exceptions;

public class MemberNotFoundException extends TowerException {

    public MemberNotFoundException(Long organizationId, String memberName) {
        super(String.format("Member '%s' not found in organization '%d'", memberName, organizationId));
    }
}
