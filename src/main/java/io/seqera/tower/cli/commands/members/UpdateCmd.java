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

package io.seqera.tower.cli.commands.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.members.MembersUpdate;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.OrgRole;
import io.seqera.tower.model.UpdateMemberRoleRequest;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "update",
        description = "Update an organization member role"
)
public class UpdateCmd extends AbstractMembersClass {

    @CommandLine.Option(names = {"-u", "--user"}, description = "Username or email address of the member to update. Specify either their platform username or email address.", required = true)
    public String user;

    @CommandLine.Option(names = {"-r", "--role"}, description = "Organization role to assign. OWNER: full administrative access including member management and billing. MEMBER: standard access with ability to create workspaces and teams. COLLABORATOR: limited access, cannot create resources but can participate in shared workspaces.", required = true)
    public OrgRole role;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        MemberDbDto member =  findMemberByUser(orgAndWorkspaceDbDto.getOrgId(), user);

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest();
        request.setRole(role);

        orgsApi().updateOrganizationMemberRole(orgAndWorkspaceDbDto.getOrgId(), member.getMemberId(), request);


        return new MembersUpdate(user, organizationRef, role.toString());
    }
}
