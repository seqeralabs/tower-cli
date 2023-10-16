/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.commands.participants;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "list",
        description = "List workspace participants."
)
public class ListCmd extends AbstractParticipantsCmd {

    @CommandLine.Option(names = {"-t", "--type"}, description = "Participant type to list (MEMBER, TEAM, COLLABORATOR).")
    public ParticipantType type;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only participants that it's name starts with the given word.")
    public String startsWith;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        Long wspId = workspaceId(workspace.workspace);

        List<ParticipantDbDto> response = api().listWorkspaceParticipants(orgId(wspId), wspId, max, offset, startsWith).getParticipants();

        if (response != null && type != null) {
            response = response.stream().filter(it -> it.getType() == type).collect(Collectors.toList());
        }

        return new ParticipantsList(orgName(wspId), workspaceName(wspId), response, PaginationInfo.from(paginationOptions));
    }
}
