package io.seqera.tower.cli.commands.members;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.members.MembersList;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list",
        description = "List all the teams of a given organization"
)
public class ListCmd extends AbstractMembersClass {

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization's name identifier", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only members with usernames that starts with the given word")
    public String startsWith;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByName(organizationName);

        ListMembersResponse response = api().listOrganizationMembers(orgAndWorkspaceDbDto.getOrgId(), null, null, startsWith);

        return new MembersList(organizationName, response.getMembers());
    }
}
