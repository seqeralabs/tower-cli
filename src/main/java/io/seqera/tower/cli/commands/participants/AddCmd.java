/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.exceptions.MemberNotFoundException;
import io.seqera.tower.cli.exceptions.ParticipantNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantAdded;
import io.seqera.tower.model.AddParticipantRequest;
import io.seqera.tower.model.AddParticipantResponse;
import io.seqera.tower.model.ParticipantType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Objects;

@CommandLine.Command(
        name = "add",
        description = "Add a new workspace participant."
)
public class AddCmd extends AbstractParticipantsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name, username or email for existing organization member.", required = true)
    public String name;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Type of participant (MEMBER, COLLABORATOR or TEAM).", required = true)
    public ParticipantType type;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the participant if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        AddParticipantRequest request = new AddParticipantRequest();

        Long wspId = workspaceId(workspace.workspace);

        if (Objects.equals(type, ParticipantType.MEMBER)) {
            try {
                request.setMemberId(findOrganizationMemberByName(orgId(wspId), name).getMemberId());
            } catch (MemberNotFoundException e) {
                request.setUserNameOrEmail(name);
            }
        } else if (Objects.equals(type, ParticipantType.TEAM)) {
            request.setTeamId(findOrganizationTeamByName(orgId(wspId), name).getTeamId());
        } else if (Objects.equals(type, ParticipantType.COLLABORATOR)) {
            try {
                request.setMemberId(findOrganizationCollaboratorByName(orgId(wspId), name).getMemberId());
            } catch (MemberNotFoundException e) {
                request.setUserNameOrEmail(name);
            }
        } else {
            throw new TowerException("Unknown participant candidate type provided.");
        }

        if (overwrite) tryDeleteParticipant(wspId, name, type);

        AddParticipantResponse response = api().createWorkspaceParticipant(orgId(wspId), wspId, request);

        return new ParticipantAdded(response.getParticipant(), workspaceName(wspId));
    }

    private void tryDeleteParticipant(Long wspId, String participantName, ParticipantType type) throws ApiException {
        try {
            deleteParticipantByNameAndType(wspId, participantName, type);
        }catch (ParticipantNotFoundException ignored) {}
    }
}
