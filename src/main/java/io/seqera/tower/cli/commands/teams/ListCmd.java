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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.TeamsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListTeamResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "list",
        description = "List organization teams"
)
public class ListCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        ListTeamResponse teamResponse = teamsApi().listOrganizationTeams(orgAndWorkspaceDbDto.getOrgId(), max, offset, null);

        return new TeamsList(organizationRef, teamResponse.getTeams(), baseOrgUrl(orgAndWorkspaceDbDto.getOrgName()), PaginationInfo.from(paginationOptions, teamResponse.getTotalSize()));
    }
}
