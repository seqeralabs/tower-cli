/*
 * Copyright 2023, Seqera.
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
import io.seqera.tower.cli.exceptions.TeamNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.TeamAdded;
import io.seqera.tower.model.CreateTeamRequest;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.Team;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "add",
        description = "Add a new organization team."
)
public class AddCmd extends AbstractTeamsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name.", required = true)
    public String teamName;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or identifier.", required = true)
    public String organizationRef;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Team description.")
    public String teamDescription;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the team if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        Team team = new Team();
        team.setName(teamName);
        team.setDescription(teamDescription);

        CreateTeamRequest request = new CreateTeamRequest();
        request.setTeam(team);

        if(overwrite) tryDeleteTeam(orgAndWorkspaceDbDto.getOrgId(), teamName);

        api().createOrganizationTeam(orgAndWorkspaceDbDto.getOrgId(), request);

        return new TeamAdded(organizationRef, teamName);
    }

    private void tryDeleteTeam(Long orgId, String teamName) throws ApiException {
        try {
            TeamDbDto team = findTeamByName(orgId, teamName);
            deleteTeamById(team.getTeamId(), orgId);
        } catch (TeamNotFoundException ignored) {}
    }
}
