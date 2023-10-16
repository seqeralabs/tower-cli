/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.OrganizationNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsAdded;
import io.seqera.tower.model.CreateOrganizationRequest;
import io.seqera.tower.model.CreateOrganizationResponse;
import io.seqera.tower.model.Organization;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "add",
        description = "Add a new organization."
)
public class AddCmd extends AbstractOrganizationsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Organization name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-f", "--full-name"}, description = "Organization full name.", required = true)
    public String fullName;

    @CommandLine.Mixin
    OrganizationsOptions opts;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the organization if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        CreateOrganizationResponse response;

        Organization organization = new Organization();
        organization.setName(name);
        organization.setFullName(fullName);
        organization.setDescription(opts.description);
        organization.setLocation(opts.location);
        organization.setWebsite(opts.website);

        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setOrganization(organization);

        if (overwrite) tryDeleteOrg(name);

        response = api().createOrganization(request);

        return new OrganizationsAdded(response.getOrganization());
    }

    private void tryDeleteOrg(final String name) throws ApiException {
        try {
            deleteOrgByName(name);
        } catch (OrganizationNotFoundException ignored){}
    }
}
