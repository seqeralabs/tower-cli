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

        AddTeamMemberResponse response = api().createOrganizationTeamMember(orgAndWorkspaceDbDto.getOrgId(), team.getTeamId(), request);

        return new TeamMembersAdd(parent.teamName, response.getMember());
    }
}
