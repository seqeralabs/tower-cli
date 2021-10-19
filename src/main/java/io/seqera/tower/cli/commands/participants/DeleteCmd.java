package io.seqera.tower.cli.commands.participants;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantDeleted;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;
import picocli.CommandLine;

@CommandLine.Command(
        name = "delete",
        description = "Delete a workspace participant"
)
public class DeleteCmd extends AbstractParticipantsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name, username or email of existing organization team or member", required = true)
    public String name;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Type or participant (MEMBER, COLLABORATOR or TEAM)", required = true)
    public ParticipantType type;

    @Override
    protected Response exec() throws ApiException, IOException {
        ParticipantDbDto participant = findWorkspaceParticipant(orgId(), workspaceId(), name, type);
        api().deleteWorkspaceParticipant(orgId(), workspaceId(), participant.getParticipantId());

        return new ParticipantDeleted(name, workspaceName());
    }
}
