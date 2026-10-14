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
import io.seqera.tower.cli.responses.organizations.IdpGroupAdded;
import io.seqera.tower.model.CreateIdpGroupRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "add",
        description = "Add an IdP group manually"
)
public class AddCmd extends AbstractApiCmd {

    @Option(names = {"-o", "--organization"}, description = "Organization name or numeric ID. Specify either the unique organization name or the numeric organization ID returned by 'tw organizations list'.", required = true)
    public String organizationRef;

    @Option(names = {"-n", "--name"}, description = "IdP group name. Must match exactly the value your IdP sends in the groups claim.", required = true)
    public String name;

    @Override
    protected Response exec() throws ApiException {
        Long orgId = findOrganizationByRef(organizationRef).getOrgId();
        return new IdpGroupAdded(organizationRef, orgsApi().createOrganizationIdpGroup(orgId, new CreateIdpGroupRequest().displayName(name)));
    }
}
