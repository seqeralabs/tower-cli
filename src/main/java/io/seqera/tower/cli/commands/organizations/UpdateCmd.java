package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsUpdated;
import io.seqera.tower.model.DescribeOrganizationResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.Organization;
import io.seqera.tower.model.OrganizationDbDto;
import io.seqera.tower.model.UpdateOrganizationRequest;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "update",
        description = "Update organization details"
)
public class UpdateCmd extends AbstractOrganizationsCmd {

    @CommandLine.Mixin
    OrganizationsOptions opts;

    @CommandLine.Option(names = {"-f", "--full-name"}, description = "Organization full name")
    public String fullName;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = organizationByName(opts.name);

        DescribeOrganizationResponse describeOrganization = api().describeOrganization(orgAndWorkspaceDbDto.getOrgId());

        OrganizationDbDto organization = describeOrganization.getOrganization();

        UpdateOrganizationRequest request = new UpdateOrganizationRequest();
        request.setFullName(fullName != null ? fullName : organization.getFullName());
        request.setDescription(opts.description != null ? opts.description : organization.getDescription());
        request.setLocation(opts.location != null ? opts.location : organization.getLocation());
        request.setWebsite(opts.website != null ? opts.website : organization.getWebsite());

        api().updateOrganization(orgAndWorkspaceDbDto.getOrgId(), request);

        return new OrganizationsUpdated(opts.name);
    }
}
