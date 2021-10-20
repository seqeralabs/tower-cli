package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantAdded;
import io.seqera.tower.model.AddParticipantRequest;
import io.seqera.tower.model.AddParticipantResponse;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.ParticipantType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Objects;

@CommandLine.Command(
        name = "add",
        description = "Create a new workspace participant"
)
public class AddCmd extends AbstractParticipantsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name or username of existing organization team or member", required = true)
    public String name;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Type or participant (MEMBER, COLLABORATOR or TEAM)", required = true)
    public ParticipantType type;

    @Override
    protected Response exec() throws ApiException, IOException {

        AddParticipantRequest request = new AddParticipantRequest();

        if (Objects.equals(type, ParticipantType.MEMBER)) {
            request.setMemberId(findOrganizationMemberByName(orgId(), name).getMemberId());
        } else if (Objects.equals(type, ParticipantType.TEAM)) {
            request.setTeamId(findOrganizationTeamByName(orgId(), name).getTeamId());
        } else if (Objects.equals(type, ParticipantType.COLLABORATOR)) {
            request.setMemberId(findOrganizationCollaboratorByName(orgId(), name).getMemberId());
        } else {
            throw new TowerException("Unknown participant candidate type provided.");
        }

        AddParticipantResponse response = api().createWorkspaceParticipant(orgId(), workspaceId(), request);

        return new ParticipantAdded(response.getParticipant(), workspaceName());
    }
}
