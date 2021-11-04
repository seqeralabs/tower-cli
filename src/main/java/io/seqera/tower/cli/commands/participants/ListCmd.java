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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantsList;
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

    @CommandLine.Mixin
    public WorkspaceOptions workspace;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Participant type to list (MEMBER, TEAM, COLLABORATOR)")
    public ParticipantType type;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only participants that it's name starts with the given word")
    public String startsWith;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        List<ParticipantDbDto> response = api().listWorkspaceParticipants(orgId(workspace.workspaceId), workspace.workspaceId, max, offset, startsWith).getParticipants();

        if (response != null && type != null) {
            response = response.stream().filter(it -> it.getType() == type).collect(Collectors.toList());
        }

        return new ParticipantsList(orgName(workspace.workspaceId), workspaceName(workspace.workspaceId), response);
    }
}
