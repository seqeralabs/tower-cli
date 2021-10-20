package io.seqera.tower.cli.commands.participants;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "leave",
        description = "Leave a workspace"
)
public class LeaveCmd extends AbstractParticipantsCmd {

    @Override
    protected Response exec() throws ApiException, IOException {
        Long workspaceId = workspaceId();
        if (workspaceId == null) {
            throw new TowerException("Missing workspace option.");
        }
        api().leaveWorkspaceParticipant(orgId(), workspaceId());
        return new ParticipantLeft(workspaceName());
    }
}
