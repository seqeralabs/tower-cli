package io.seqera.tower.cli.commands.teams.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.teams.MembersCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMembersAdd;
import io.seqera.tower.model.AddTeamMemberResponse;
import io.seqera.tower.model.CreateTeamMemberRequest;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "add",
        description = "Adds a team's members"
)
public class AddCmd extends AbstractApiCmd {

    @CommandLine.Option(names = {"-m", "--member"}, description = "New member username or email", required = true)
    public String userNameOrEmail;

    @CommandLine.ParentCommand
    public MembersCmd parent;

    public Tower app() {
        return parent.app();
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = parent.findOrganizationByName(parent.organizationName);

        TeamDbDto team = parent.findTeamByName(orgAndWorkspaceDbDto.getOrgId(), parent.teamName);

        CreateTeamMemberRequest request = new CreateTeamMemberRequest();
        request.setUserNameOrEmail(userNameOrEmail);

        AddTeamMemberResponse response = api().createOrganizationTeamMember(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId(), request);

        return new TeamMembersAdd(parent.teamName, response.getMember());
    }
}
