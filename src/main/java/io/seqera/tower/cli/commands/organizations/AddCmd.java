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
