package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantChanged;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;
import io.seqera.tower.model.UpdateParticipantRoleRequest;
import io.seqera.tower.model.WspRole;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "change",
        description = "Update a participant role"
)
public class ChangeCmd extends AbstractParticipantsCmd {

    @CommandLine.Mixin
    ParticipantsOptions opts;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name, username or email of existing organization team or member", required = true)
    public String name;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Type or participant (MEMBER, COLLABORATOR or TEAM)", required = true)
    public ParticipantType type;

    @CommandLine.Option(names = {"-r", "--role"}, description = "Workspace participant role (OWNER, ADMIN, MAINTAIN, LAUNCH or VIEW)", required = true)
    public WspRole role;

    @Override
    protected Response exec() throws ApiException, IOException {
        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto = findOrgAndWorkspaceByName(opts.organizationName, opts.workspaceName);

        ParticipantDbDto participant = findWorkspaceParticipant(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId(), name, type);

        UpdateParticipantRoleRequest request = new UpdateParticipantRoleRequest();
        request.setRole(role);

        api().updateWorkspaceParticipantRole(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId(), participant.getParticipantId(), request);

        return new ParticipantChanged(opts.workspaceName, name, role.getValue());
    }
}
