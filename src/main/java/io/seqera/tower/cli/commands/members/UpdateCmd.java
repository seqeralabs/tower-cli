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

package io.seqera.tower.cli.commands.members;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.members.MembersUpdate;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.OrgRole;
import io.seqera.tower.model.UpdateMemberRoleRequest;
import picocli.CommandLine;

@CommandLine.Command(
        name = "update",
        description = "Update the role of an organization member."
)
public class UpdateCmd extends AbstractMembersClass {

    @CommandLine.Option(names = {"-u", "--user"}, description = "Username or email to update from organization members.", required = true)
    public String user;

    @CommandLine.Option(names = {"-r", "--role"}, description = "Member organization role (OWNER, MEMBER or COLLABORATOR).", required = true)
    public OrgRole role;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or identifier.", required = true)
    public String organizationRef;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        MemberDbDto member =  findMemberByUser(orgAndWorkspaceDbDto.getOrgId(), user);

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest();
        request.setRole(role);

        api().updateOrganizationMemberRole(orgAndWorkspaceDbDto.getOrgId(), member.getMemberId(), request);


        return new MembersUpdate(user, organizationRef, role.toString());
    }
}
