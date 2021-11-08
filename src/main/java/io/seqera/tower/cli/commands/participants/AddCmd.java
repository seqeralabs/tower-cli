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
import io.seqera.tower.cli.commands.global.WorkspaceOptions;
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
        description = "Create a new workspace participant"
)
public class AddCmd extends AbstractParticipantsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name or username of existing organization team or member", required = true)
    public String name;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Type or participant (MEMBER, COLLABORATOR or TEAM)", required = true)
    public ParticipantType type;

    @CommandLine.Mixin
    public WorkspaceOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {

        AddParticipantRequest request = new AddParticipantRequest();

        if (Objects.equals(type, ParticipantType.MEMBER)) {
            request.setMemberId(findOrganizationMemberByName(orgId(workspace.workspaceId), name).getMemberId());
        } else if (Objects.equals(type, ParticipantType.TEAM)) {
            request.setTeamId(findOrganizationTeamByName(orgId(workspace.workspaceId), name).getTeamId());
        } else if (Objects.equals(type, ParticipantType.COLLABORATOR)) {
            request.setMemberId(findOrganizationCollaboratorByName(orgId(workspace.workspaceId), name).getMemberId());
        } else {
            throw new TowerException("Unknown participant candidate type provided.");
        }

        AddParticipantResponse response = api().createWorkspaceParticipant(orgId(workspace.workspaceId), workspace.workspaceId, request);

        return new ParticipantAdded(response.getParticipant(), workspaceName(workspace.workspaceId));
    }
}
