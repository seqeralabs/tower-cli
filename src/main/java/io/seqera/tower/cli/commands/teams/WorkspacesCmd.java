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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.TeamWorkspacesList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListWorkspacesByTeamResponse;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "workspaces",
        description = "List the workspaces a team participates in"
)
public class WorkspacesCmd extends AbstractTeamsCmd {

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Mixin
    TeamRefOptions ref;

    @Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        TeamDbDto team = fetchTeam(orgId, ref);

        ListWorkspacesByTeamResponse response = teamsApi().listWorkspacesByTeam(orgId, team.getTeamId(), max, offset, null);

        return new TeamWorkspacesList(organizationRef, team.getName(), response.getWorkspaces(), PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }
}
