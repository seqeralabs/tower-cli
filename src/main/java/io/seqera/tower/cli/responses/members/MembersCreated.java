package io.seqera.tower.cli.responses.members;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.MemberDbDto;

public class MembersCreated extends Response {

    public final String orgName;
    public final MemberDbDto member;

    public MembersCreated(String orgName, MemberDbDto member) {
        this.orgName = orgName;
        this.member = member;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Member '%s' with ID '%d' was created in organization '%s'|@%n", member.getUserName(), member.getMemberId(), orgName));
    }
}
