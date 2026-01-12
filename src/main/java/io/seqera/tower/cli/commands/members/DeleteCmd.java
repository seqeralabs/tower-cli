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
import io.seqera.tower.cli.responses.members.MembersDeleted;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Remove an organization member"
)
public class DeleteCmd extends AbstractMembersClass{

    @CommandLine.Option(names = {"-u", "--user"}, description = "Username or email address", required = true)
    public String user;

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization name or identifier", required = true)
    public String organizationRef;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDto orgAndWorkspaceDbDto = findOrganizationByRef(organizationRef);

        MemberDbDto member = findMemberByUser(orgAndWorkspaceDbDto.getOrgId(), user);

        orgsApi().deleteOrganizationMember(orgAndWorkspaceDbDto.getOrgId(), member.getMemberId());

        return new MembersDeleted(user, organizationRef);
    }
}
