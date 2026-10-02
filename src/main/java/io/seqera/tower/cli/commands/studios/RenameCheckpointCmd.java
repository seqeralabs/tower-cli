/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.studios;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.studios.StudioCheckpointUpdated;
import io.seqera.tower.model.DataStudioCheckpointDto;
import io.seqera.tower.model.DataStudioCheckpointUpdateRequest;
import picocli.CommandLine;

@CommandLine.Command(
        name = "rename-checkpoint",
        description = "Rename a studio checkpoint."
)
public class RenameCheckpointCmd extends AbstractStudiosCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public StudioRefOptions studioRefOptions;

    @CommandLine.Option(names = {"--checkpoint-id"}, description = "Checkpoint numeric identifier, as listed by the 'checkpoints' command.", required = true)
    public Long checkpointId;

    @CommandLine.Option(names = {"--new-name"}, description = "New checkpoint name. Must be unique per studio.", required = true)
    public String newName;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String sessionId = getSessionId(studioRefOptions, wspId);

        DataStudioCheckpointUpdateRequest request = new DataStudioCheckpointUpdateRequest();
        request.setName(newName);
        DataStudioCheckpointDto checkpoint = studiosApi().updateDataStudioCheckpoint(sessionId, checkpointId, request, wspId);

        return new StudioCheckpointUpdated(studioRefOptions.getStudioIdentifier(), workspaceRef(wspId), checkpoint.getId(), checkpoint.getName());
    }
}
