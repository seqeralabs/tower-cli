/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.teams.members.AddCmd;
import io.seqera.tower.cli.commands.teams.members.DeleteCmd;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMembersList;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "members",
        description = "List all team members.",
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
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        TeamDbDto team = findTeamByName(orgAndWorkspaceDbDto.getOrgId(), teamName);

        //TODO use pagination to get all the members
        ListMembersResponse response = teamsApi().listOrganizationTeamMembers(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId(), 100, 0, null);

        if (response == null) {
            throw new TowerException(String.format("Team '%s' has no members", teamName));
        }

        return new TeamMembersList(teamName, response.getMembers());
    }
}
