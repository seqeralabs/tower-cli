package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsCreated;
import io.seqera.tower.model.CreateOrganizationRequest;
import io.seqera.tower.model.CreateOrganizationResponse;
import io.seqera.tower.model.Organization;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "create",
        description = "Create a new organization"
)
public class CreateCmd extends AbstractOrganizationsCmd {

    @CommandLine.Mixin
    OrganizationsOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        CreateOrganizationResponse response;

        try {
            Organization organization = new Organization();
            organization.setName(opts.name);
            organization.setFullName(opts.fullName);
            organization.setDescription(opts.description);
            organization.setLocation(opts.location);
            organization.setWebsite(opts.website);

            CreateOrganizationRequest request = new CreateOrganizationRequest();
            request.setOrganization(organization);

            response = api().createOrganization(request);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to create organization '%s'", opts.name));
        }

        return new OrganizationsCreated(response.getOrganization());
    }
}
