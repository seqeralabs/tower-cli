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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.TeamUpdated;
import io.seqera.tower.model.TeamDbDto;
import io.seqera.tower.model.UpdateTeamRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        name = "update",
        description = "Update a team"
)
public class UpdateCmd extends AbstractTeamsCmd {

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Mixin
    TeamRefOptions ref;

    @Option(names = {"--new-name"}, description = "New team name. Must be unique within the organization.")
    public String newName;

    @Option(names = {"-d", "--description"}, description = "New team description.")
    public String description;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        TeamDbDto team = fetchTeam(orgId, ref);

        // The API replaces name, description and avatar, so unset options keep the current values
        UpdateTeamRequest request = new UpdateTeamRequest()
                .name(newName != null ? newName : team.getName())
                .description(description != null ? description : team.getDescription())
                .avatarId(avatarId(team.getAvatarUrl()));

        teamsApi().updateOrganizationTeam(orgId, team.getTeamId(), request);

        return new TeamUpdated(organizationRef, request.getName());
    }

    // The team response only exposes the avatar download URL, which ends with the avatar ID
    private static String avatarId(String avatarUrl) {
        return avatarUrl == null ? null : avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
    }
}
