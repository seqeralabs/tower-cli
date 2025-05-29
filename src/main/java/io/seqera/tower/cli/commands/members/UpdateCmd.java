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
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        MemberDbDto member =  findMemberByUser(orgAndWorkspaceDbDto.getOrgId(), user);

        UpdateMemberRoleRequest request = new UpdateMemberRoleRequest();
        request.setRole(role);

        orgsApi().updateOrganizationMemberRole(orgAndWorkspaceDbDto.getOrgId(), member.getMemberId(), request);


        return new MembersUpdate(user, organizationRef, role.toString());
    }
}
