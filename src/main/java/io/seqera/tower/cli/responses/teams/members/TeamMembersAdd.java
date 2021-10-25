package io.seqera.tower.cli.responses.teams.members;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.MemberDbDto;

public class TeamMembersAdd extends Response {

    Long teamId;
    MemberDbDto member;

    public TeamMembersAdd(Long teamId, MemberDbDto member) {
        this.teamId = teamId;
        this.member = member;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' added to team '%d' with id '%d'|@%n", member.getUserName(), teamId, member.getMemberId()));
    }

}
