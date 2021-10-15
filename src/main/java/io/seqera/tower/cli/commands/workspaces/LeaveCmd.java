package io.seqera.tower.cli.commands.workspaces;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "leave",
        description = "Leave workspace"
)
public class LeaveCmd extends AbstractWorkspaceCmd{

    @Override
    protected Response exec() throws ApiException, IOException {
        String workspaceName = workspaceName();

        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = workspaceById(workspaceId());

        api().leaveWorkspaceParticipant(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new ParticipantLeft(workspaceName);
    }
}
