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

package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsUpdated;
import io.seqera.tower.model.DescribeOrganizationResponse;
import io.seqera.tower.model.OrganizationDbDto;
import io.seqera.tower.model.UpdateOrganizationRequest;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "update",
        description = "Update an organization"
)
public class UpdateCmd extends AbstractOrganizationsCmd {

    @CommandLine.Mixin
    OrganizationRefOptions organizationRefOptions;

    @CommandLine.Option(names = {"--new-name"}, description = "New name for the organization")
    public String newName;

    @CommandLine.Option(names = {"-f", "--full-name"}, description = "Organization full name")
    public String fullName;

    @CommandLine.Mixin
    OrganizationsOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        DescribeOrganizationResponse response = fetchOrganization(organizationRefOptions);

        OrganizationDbDto organization = response.getOrganization();

        UpdateOrganizationRequest request = new UpdateOrganizationRequest();
        request.setName(newName != null ? newName : organization.getName());
        request.setFullName(fullName != null ? fullName : organization.getFullName());
        request.setDescription(opts.description != null ? opts.description : organization.getDescription());
        request.setLocation(opts.location != null ? opts.location : organization.getLocation());
        request.setWebsite(opts.website != null ? opts.website : organization.getWebsite());

        orgsApi().updateOrganization(organization.getOrgId(), request);

        return new OrganizationsUpdated(organization.getOrgId(), organization.getName());
    }
}
