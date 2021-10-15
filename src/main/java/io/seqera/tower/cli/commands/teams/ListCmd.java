package io.seqera.tower.cli.commands.teams;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.TeamsList;
import io.seqera.tower.model.ListTeamResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        description = "List all the teams of a given organization"
)
public class ListCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization's name identifier", required = true)
    public String organizationName;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByName(organizationName);

        ListTeamResponse teamResponse = api().listOrganizationTeams(orgAndWorkspaceDbDto.getOrgId(), null, null);

        return new TeamsList(organizationName, teamResponse.getTeams());
    }
}
