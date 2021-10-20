package io.seqera.tower.cli.exceptions;

public class TeamNotFoundException extends TowerException {

    public TeamNotFoundException(Long organizationId, String memberName) {
        super(String.format("Team '%s' not found in organization '%d'", memberName, organizationId));
    }
}
