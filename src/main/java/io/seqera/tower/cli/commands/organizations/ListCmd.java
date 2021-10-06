package io.seqera.tower.cli.commands.organizations;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.organizations.OrganizationsList;
import io.seqera.tower.model.ListWorkspacesAndOrgResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "list",
        description = "List available organizations"
)
public class ListCmd extends AbstractOrganizationsCmd {
    @Override
    protected Response exec() throws ApiException, IOException {

        ListWorkspacesAndOrgResponse response = api().listWorkspacesUser(userId());

        if (response.getOrgsAndWorkspaces() != null) {
            List<OrgAndWorkspaceDbDto> responseOrg = response.getOrgsAndWorkspaces()
                    .stream()
                    .filter(item -> Objects.equals(item.getWorkspaceId(), null))
                    .collect(Collectors.toList());

            return new OrganizationsList(userName(), responseOrg);
        }

        return new OrganizationsList(userName(), response.getOrgsAndWorkspaces());
    }
}
