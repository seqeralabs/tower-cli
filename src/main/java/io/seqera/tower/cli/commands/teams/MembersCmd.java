/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

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
        description = "Add or delete team members.",
        subcommands = {
                AddCmd.class,
                DeleteCmd.class,
        }
)
public class MembersCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-t", "--team"}, description = "Team name.", required = true)
    public String teamName;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or identifier.", required = true)
    public String organizationRef;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        TeamDbDto team = findTeamByName(orgAndWorkspaceDbDto.getOrgId(), teamName);

        ListMembersResponse response = api().listOrganizationTeamMembers(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId());

        if (response == null) {
            throw new TowerException(String.format("Team '%s' has no members", teamName));
        }

        return new TeamMembersList(teamName, response.getMembers());
    }
}
