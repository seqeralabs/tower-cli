package io.seqera.tower.cli.commands.participants;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "leave",
        description = "Leave a workspace"
)
public class LeaveCmd extends AbstractParticipantsCmd {

    @CommandLine.Mixin
    ParticipantsOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrgAndWorkspaceByName(opts.organizationName, opts.workspaceName);

        api().leaveWorkspaceParticipant(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new ParticipantLeft(opts.workspaceName);
    }
}
