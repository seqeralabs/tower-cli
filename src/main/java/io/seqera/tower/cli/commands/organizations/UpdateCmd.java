package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsUpdated;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
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

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = organizationByName(opts.name);

        try {
            UpdateOrganizationRequest request = new UpdateOrganizationRequest();
            request.setFullName(opts.fullName);
            request.setDescription(opts.description);
            request.setLocation(opts.fullName);
            request.setWebsite(opts.website);

            api().updateOrganization(orgAndWorkspaceDbDto.getOrgId(), request);
        } catch (Exception e) {
            throw new TowerException(String.format("Unable to update organization '%s'", opts.name));
        }

        return new OrganizationsUpdated(opts.name);
    }
}
