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
        description = "Add an organization"
)
public class AddCmd extends AbstractOrganizationsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Organization unique name. Must be unique across Seqera Platform. Used as the organization identifier in URLs and API calls. Cannot be changed after creation without --new-name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-f", "--full-name"}, description = "Organization display name. The full, human-readable name for the organization shown in the UI. Can contain spaces and special characters.", required = true)
    public String fullName;

    @CommandLine.Mixin
    OrganizationsOptions opts;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite existing organization. If an organization with this name already exists, delete it first before creating the new one. Use with caution as this permanently deletes the existing organization and all associated data.", defaultValue = "false")
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

        response = orgsApi().createOrganization(request);

        return new OrganizationsAdded(response.getOrganization());
    }

    private void tryDeleteOrg(final String name) throws ApiException {
        try {
            deleteOrgByName(name);
        } catch (OrganizationNotFoundException ignored){}
    }
}
