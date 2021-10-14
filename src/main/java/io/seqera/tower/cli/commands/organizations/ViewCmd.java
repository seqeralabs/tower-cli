package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsView;
import io.seqera.tower.model.DescribeOrganizationResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "view",
        description = "Describe organization details"
)
public class ViewCmd extends AbstractOrganizationsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Organization name", required = true)
    public String name;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = organizationByName(name);

        DescribeOrganizationResponse response = api().describeOrganization(orgAndWorkspaceDbDto.getOrgId());

        return new OrganizationsView(response.getOrganization());
    }
}
