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

package io.seqera.tower.cli.commands.organizations.idpgroups;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.IdpGroupDeleted;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "delete",
        description = "Delete a manually added IdP group. Groups pushed by SCIM can't be deleted."
)
public class DeleteCmd extends AbstractApiCmd {

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @ArgGroup(multiplicity = "1")
    GroupRef group;

    static class GroupRef {
        @Option(names = {"-i", "--id"}, description = "IdP group numeric identifier.")
        Long id;

        @Option(names = {"-n", "--name"}, description = "IdP group name.")
        String name;
    }

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        Long groupId = group.id != null ? group.id : findIdpGroupByName(orgId, group.name).getId();

        orgsApi().deleteOrganizationIdpGroup(orgId, groupId);

        return new IdpGroupDeleted(organizationRef, group.name != null ? group.name : groupId.toString());
    }
}
