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
public class LeaveCmd extends AbstractWorkspaceCmd {

    @CommandLine.ArgGroup(exclusive = false, heading = "%nMatch by workspace and organization name:%n")
    WorkspacesMatchOptions.MatchByName matchByName;

    @CommandLine.ArgGroup(heading = "%nMatch by workspace ID:%n")
    WorkspacesMatchOptions.MatchById matchById;

    @Override
    protected Response exec() throws ApiException, IOException {

        OrgAndWorkspaceDbDto orgAndWorkspaceDbDto;

        if (matchById != null) {
            orgAndWorkspaceDbDto = workspaceById(matchById.workspaceId);
        } else {
            orgAndWorkspaceDbDto = orgAndWorkspaceByName(matchByName.workspaceName, matchByName.organizationName);
        }

        api().leaveWorkspaceParticipant(orgAndWorkspaceDbDto.getOrgId(), orgAndWorkspaceDbDto.getWorkspaceId());

        return new ParticipantLeft(orgAndWorkspaceDbDto.getWorkspaceName());
    }
}
