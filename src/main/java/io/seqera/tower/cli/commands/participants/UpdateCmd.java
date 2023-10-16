/*
 * Copyright 2023, Seqera.
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
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.participants.ParticipantUpdated;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ParticipantType;
import io.seqera.tower.model.UpdateParticipantRoleRequest;
import io.seqera.tower.model.WspRole;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "update",
        description = "Update a participant role."
)
public class UpdateCmd extends AbstractParticipantsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Team name, username or email for existing organization member.", required = true)
    public String name;

    @CommandLine.Option(names = {"-t", "--type"}, description = "Type of participant (MEMBER, COLLABORATOR or TEAM).", required = true)
    public ParticipantType type;

    @CommandLine.Option(names = {"-r", "--role"}, description = "Workspace participant role (OWNER, ADMIN, MAINTAIN, LAUNCH or VIEW).", required = true)
    public WspRole role;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        ParticipantDbDto participant = findWorkspaceParticipant(orgId(wspId), wspId, name, type);

        UpdateParticipantRoleRequest request = new UpdateParticipantRoleRequest();
        request.setRole(role);

        api().updateWorkspaceParticipantRole(orgId(wspId), wspId, participant.getParticipantId(), request);

        return new ParticipantUpdated(workspaceName(wspId), name, role.getValue());
    }
}
