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
import io.seqera.tower.cli.responses.participants.ParticipantLeft;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "leave",
        description = "Leave a workspace"
)
public class LeaveCmd extends AbstractParticipantsCmd {

    @CommandLine.Mixin
    public WorkspaceOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long workspaceId = workspace.workspaceId;
        if (workspaceId == null) {
            throw new TowerException("Missing workspace option.");
        }
        api().leaveWorkspaceParticipant(orgId(workspace.workspaceId), workspace.workspaceId);
        return new ParticipantLeft(workspaceName(workspace.workspaceId));
    }
}
