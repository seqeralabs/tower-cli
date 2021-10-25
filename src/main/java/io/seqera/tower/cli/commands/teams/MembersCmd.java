package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.teams.members.AddCmd;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMembersList;
import io.seqera.tower.model.ListMembersResponse;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "members",
        description = "Operates over team's members",
        subcommands = {
                AddCmd.class,
        }
)
public class MembersCmd extends AbstractTeamsCmd {
    @CommandLine.Option(names = {"-i", "--id"}, description = "Team's id", required = true)
    public Long teamId;

    @Override
    protected Response exec() throws ApiException, IOException {
        ListMembersResponse response = api().listOrganizationTeamMembers(orgId(), teamId);

        if (response == null) {
            throw new TowerException(String.format("Team '%d' has no members", teamId));
        }

        return new TeamMembersList(teamId, response.getMembers());
    }
}
