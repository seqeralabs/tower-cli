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

package io.seqera.tower.cli.commands.teams.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.teams.MembersCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.teams.members.TeamMembersAdd;
import io.seqera.tower.model.AddTeamMemberResponse;
import io.seqera.tower.model.CreateTeamMemberRequest;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.TeamDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "add",
        description = "Add a team member."
)
public class AddCmd extends AbstractApiCmd {

    @CommandLine.Option(names = {"-m", "--member"}, description = "New member username or email.", required = true)
    public String userNameOrEmail;

    @CommandLine.ParentCommand
    public MembersCmd parent;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(parent.organizationRef);

        TeamDbDto team = parent.findTeamByName(orgAndWorkspaceDbDto.getOrgId(), parent.teamName);

        CreateTeamMemberRequest request = new CreateTeamMemberRequest();
        request.setUserNameOrEmail(userNameOrEmail);

        AddTeamMemberResponse response = teamsApi().createOrganizationTeamMember(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId(), request);

        return new TeamMembersAdd(parent.teamName, response.getMember());
    }
}
