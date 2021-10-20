package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantsList;
import io.seqera.tower.model.OrgAndWorkspaceDbDto;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "list",
        description = "List workspace participants"
)
public class ListCmd extends AbstractParticipantsCmd {

    @CommandLine.Option(names = {"-t", "--type"}, description = "Participant type to list (MEMBER, TEAM, COLLABORATOR)")
    public ParticipantType type;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only participants that it's name starts with the given word")
    public String startsWith;

    @Override
    protected Response exec() throws ApiException, IOException {
        List<ParticipantDbDto> response = api().listWorkspaceParticipants(orgId(), workspaceId(), null, null, startsWith).getParticipants();

        if (response != null && type != null) {
            response = response.stream().filter(it -> it.getType() == type).collect(Collectors.toList());
        }

        return new ParticipantsList(orgName(), workspaceName(), response);
    }
}
