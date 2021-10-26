package io.seqera.tower.cli.responses.teams.members;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.MemberDbDto;

public class TeamMembersAdd extends Response {

    String teamName;
    MemberDbDto member;

    public TeamMembersAdd(String teamName, MemberDbDto member) {
        this.teamName = teamName;
        this.member = member;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' added to team '%s' with id '%d'|@%n", member.getUserName(), teamName, member.getMemberId()));
    }

}
