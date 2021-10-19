package io.seqera.tower.cli.commands.members;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.members.MembersDeleted;
import io.seqera.tower.model.MemberDbDto;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "delete",
        description = "Delete an organization member"
)
public class DeleteCmd extends AbstractMembersClass{

    @CommandLine.Option(names = {"-o", "--organization"}, description = "Organization's name identifier", required = true)
    public String organizationName;

    @CommandLine.Option(names = {"-u", "--user"}, description = "Username or email of user to delete from organization's members")
    public String user;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrganizationByName(organizationName);

        MemberDbDto member = findMemberByUser(orgAndWorkspaceDbDto.getOrgId(), user);

        api().deleteOrganizationMember(orgAndWorkspaceDbDto.getOrgId(), member.getMemberId());

        return new MembersDeleted(user, organizationName);
    }
}
