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
