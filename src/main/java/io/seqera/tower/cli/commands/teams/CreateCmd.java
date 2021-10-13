package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.TeamCreated;
import io.seqera.tower.model.CreateTeamRequest;
import io.seqera.tower.model.CreateTeamResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.Team;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "create",
        description = "Create a new organization team"
)
public class CreateCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization's name identifier", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team's name", required = true)
    public String teamName;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Team's description")
    public String teamDescription;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByName(organizationName);

        Team team = new Team();
        team.setName(teamName);
        team.setDescription(teamDescription);

        CreateTeamRequest request = new CreateTeamRequest();
        request.setTeam(team);

        CreateTeamResponse response = api().createOrganizationTeam(orgAndWorkspaceDbDto.getOrgId(), request);

        return new TeamCreated(organizationName, teamName);
    }
}
