package io.seqera.tower.cli.commands.teams.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.teams.AbstractTeamsCmd;
import io.seqera.tower.cli.commands.teams.MembersCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMemberDeleted;
import io.seqera.tower.model.MemberDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a team's members"
)
public class DeleteCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-m", "--member"}, description = "Member username to remove from team", required = true)
    public String username;

    @CommandLine.ParentCommand
    public MembersCmd parent;

    public Tower app() {
        return parent.app();
    }

    @Override
    protected Response exec() throws ApiException, IOException {

        MemberDbDto member = findMemberByUsername(orgId(), parent.teamId, username);

        api().deleteOrganizationTeamMember(orgId(), parent.teamId, member.getMemberId());

        return new TeamMemberDeleted(parent.teamId, username);
    }
}
