package io.seqera.tower.cli.commands.teams.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.teams.MembersCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMemberDeleted;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete a team's members"
)
public class DeleteCmd extends AbstractApiCmd {

    @CommandLine.Option(names = {"-m", "--member"}, description = "Member username to remove from team", required = true)
    public String username;

    @CommandLine.ParentCommand
    public MembersCmd parent;

    public Tower app() {
        return parent.app();
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = parent.findOrganizationByName(parent.organizationName);

        TeamDbDto team = parent.findTeamByName(orgAndWorkspaceDbDto.getOrgId(), parent.teamName);

        MemberDbDto member = parent.findMemberByUsername(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId(), username);

        api().deleteOrganizationTeamMember(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId(), member.getMemberId());

        return new TeamMemberDeleted(team.getName(), username);
    }
}
