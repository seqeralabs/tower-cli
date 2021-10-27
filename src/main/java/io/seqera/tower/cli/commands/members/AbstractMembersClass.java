package io.seqera.tower.cli.commands.members;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.MembersCmd;
import io.seqera.tower.cli.exceptions.MembersMultiplicityException;
import io.seqera.tower.cli.exceptions.MembersNotFoundException;
import io.seqera.tower.model.ListMembersResponse;
import io.seqera.tower.model.MemberDbDto;
import picocli.CommandLine;

@CommandLine.Command
abstract public class AbstractMembersClass extends AbstractApiCmd {

    public AbstractMembersClass() {
    }

    protected MemberDbDto findMemberByUser(Long orgId, String user) throws ApiException {
        ListMembersResponse response = api().listOrganizationMembers(orgId, null, null, user);

        if(response.getMembers() == null || response.getMembers().size() == 0){
            throw new MembersNotFoundException(user, orgId);
        }

        if(response.getMembers().size() > 1){
            throw new MembersMultiplicityException(user, orgId);
        }

        return response.getMembers().stream().findFirst().orElse(null);
    }
}
