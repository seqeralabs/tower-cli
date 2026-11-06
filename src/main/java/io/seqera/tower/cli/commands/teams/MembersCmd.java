/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.teams;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.teams.members.AddCmd;
import io.seqera.tower.cli.commands.teams.members.DeleteCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMembersList;
import io.seqera.tower.cli.utils.Paginator;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;

@CommandLine.Command(
        name = "members",
        description = "List team members",
        subcommands = {
                AddCmd.class,
                DeleteCmd.class,
        }
)
public class MembersCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-t", "--team"}, description = "Team name. The unique team identifier within the organization. Lists all members who belong to this team.", required = true)
    public String teamName;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        Long orgId = orgAndWorkspaceDbDto.getOrgId();
        TeamDbDto team = findTeamByName(orgId, teamName);

        List<MemberDbDto> members = Paginator.fetchAll(
                (max, offset) -> teamsApi().listOrganizationTeamMembers(orgId, team.getTeamId(), max, offset, null),
                ListMembersResponse::getMembers,
                ListMembersResponse::getTotalSize
        );

        return new TeamMembersList(teamName, members);
    }
}
