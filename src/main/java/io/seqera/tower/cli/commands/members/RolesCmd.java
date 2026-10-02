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
import io.seqera.tower.cli.responses.members.MemberRolesList;
import io.seqera.tower.model.MemberDbDto;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "roles",
        description = "List the workspace roles of an organization member, granted directly or through teams"
)
public class RolesCmd extends AbstractMembersClass {

    @Option(names = {"-u", "--user"}, description = "Username or email address of the organization member.", required = true)
    public String user;

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        MemberDbDto member = findMemberByUser(orgId, user);
        return new MemberRolesList(organizationRef, orgsApi().listUserRolesInOrganization(orgId, member.getUserId()));
    }
}
