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
