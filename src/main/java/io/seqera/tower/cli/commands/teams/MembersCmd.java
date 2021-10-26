package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.teams.members.AddCmd;
import io.seqera.tower.cli.commands.teams.members.DeleteCmd;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMembersList;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "members",
        description = "Operates over team's members",
        subcommands = {
                AddCmd.class,
                DeleteCmd.class,
        }
)
public class MembersCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization's name identifier", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-t", "--team"}, description = "Team's name", required = true)
    public String teamName;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByName(organizationName);

        TeamDbDto team = findTeamByName(orgAndWorkspaceDbDto.getOrgId(), teamName);

        ListMembersResponse response = api().listOrganizationTeamMembers(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId());

        if (response == null) {
            throw new TowerException(String.format("Team '%s' has no members", teamName));
        }

        return new TeamMembersList(teamName, response.getMembers());
    }
}
