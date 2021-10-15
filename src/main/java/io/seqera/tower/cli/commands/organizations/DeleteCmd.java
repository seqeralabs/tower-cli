package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsDeleted;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "delete",
        description = "Delete an organization"
)
public class DeleteCmd extends AbstractOrganizationsCmd {
    @CommandLine.Option(names = {"-n", "--name"}, description = "Organization name", required = true)
    public String name;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = organizationByName(name);

        try {
            api().deleteOrganization(orgAndWorkspaceDbDto.getOrgId());
        } catch (Exception e) {
            throw new TowerException(String.format("Organization %s could not be deleted", name));
        }

        return new OrganizationsDeleted(name);
    }
}
