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
import io.seqera.tower.cli.commands.labels.Label;
import io.seqera.tower.cli.commands.labels.LabelsOptionalOptions;
import io.seqera.tower.cli.exceptions.InvalidDataStudioParentCheckpointException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.studios.StudiosCreated;
import io.seqera.tower.model.DataStudioCheckpointDto;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.model.DataStudioCreateRequest;
import io.seqera.tower.model.DataStudioCreateResponse;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioListCheckpointsResponse;
import io.seqera.tower.model.DataStudioStatus;
import picocli.CommandLine;

import java.util.List;
import java.util.Objects;

@CommandLine.Command(
        name = "add-as-new",
        description = "Add a studio from an existing one"
)
public class AddAsNewCmd extends AbstractStudiosCmd{

    @CommandLine.Mixin
    public ParentStudioRefOptions parentStudioRefOptions;

    @CommandLine.Option(names = {"--parent-checkpoint-id"}, description = "Parent Studio checkpoint id, to be used as the starting point for the new Studio session. If not provided, it defaults to the most recent existing checkpoint of the parent Studio session.")
    public String parentCheckpointId;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Studio name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Studio description")
    public String description;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public StudioConfigurationOptions studioConfigOptions;

    @CommandLine.Option(names = {"-a", "--auto-start"}, description = "Create studio and start it immediately (default: false)", defaultValue = "false")
    public Boolean autoStart;

    @CommandLine.Option(names = {"--private"}, description = "Create a private studio that only you can access or manage (default: false)", defaultValue = "false")
    public Boolean isPrivate;

    @CommandLine.Option(names = {"--labels"}, description = "Comma-separated list of labels", split = ",", converter = Label.StudioResourceLabelsConverter.class)
    public List<Label> labels;

    @CommandLine.Option(names = {"--wait"}, description = "Wait until Studio is in RUNNING status. Valid options: ${COMPLETION-CANDIDATES}.")
    public DataStudioStatus wait;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        try {
            String parentStudioSessionId = getParentStudioSessionId(parentStudioRefOptions, wspId);
            DataStudioDto parentStudio = studiosApi().describeDataStudio(parentStudioSessionId, wspId);
            if (parentStudio == null) {
                throw new TowerException(String.format("Parent Studio %s not found at %s workspace", parentStudioSessionId, wspId));
            }

            DataStudioCreateRequest request = prepareRequest(parentStudio, parentCheckpointId, wspId);
            DataStudioCreateResponse response = studiosApi().createDataStudio(request, wspId, autoStart);
            DataStudioDto studioDto = response.getStudio();
            assert studioDto != null;
            return new StudiosCreated(studioDto.getSessionId(), wspId, workspaceRef(wspId), baseWorkspaceUrl(wspId), autoStart);
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                throw new TowerException(String.format("User not entitled to create studio at %s workspace", wspId));
            }
            throw e;
        }
    }

    DataStudioCreateRequest prepareRequest(DataStudioDto parentDataStudio, String parentCheckpointId, Long wspId) throws ApiException {
        DataStudioCreateRequest request = new DataStudioCreateRequest();
        request.setName(name);
        if (description == null || description.isEmpty()) {
            request.description(String.format("Started from studio %s", parentDataStudio.getName()));
        } else {
            request.description(description);
        }
        request.setLabelIds(getLabelIds(labels, wspId));
        request.setIsPrivate(isPrivate);
        if (parentCheckpointId == null) {
            DataStudioListCheckpointsResponse response = studiosApi().listDataStudioCheckpoints(parentDataStudio.getSessionId(), parentDataStudio.getWorkspaceId(), null, 1, null);
            if (!response.getCheckpoints().isEmpty()) {
                request.setInitialCheckpointId(response.getCheckpoints().get(0).getId());
            }
        } else {
            try {
                Long checkpoint = Long.valueOf(parentCheckpointId);
                DataStudioCheckpointDto response = studiosApi().getDataStudioCheckpoint(parentDataStudio.getSessionId(), checkpoint, wspId);
                request.setInitialCheckpointId(response.getId());
            } catch (NumberFormatException | ApiException e) {
                throw new InvalidDataStudioParentCheckpointException(parentCheckpointId);
            }
        }

        request.setDataStudioToolUrl(Objects.requireNonNull(parentDataStudio.getTemplate()).getRepository());
        request.setComputeEnvId(Objects.requireNonNull(parentDataStudio.getComputeEnv()).getId());

        DataStudioConfiguration newConfig = studioConfigurationFrom(workspaceId(workspace.workspace), parentDataStudio, studioConfigOptions);

        request.setConfiguration(newConfig);
        return request;
    }

    @Override
    protected Integer onBeforeExit(int exitCode, Response response) {

        if (!autoStart) {
            return exitCode;
        }

        if (exitCode != 0 || wait == null || response == null) {
            return exitCode;
        }

        StudiosCreated createdResponse = ((StudiosCreated) response);
        return onBeforeExit(exitCode, createdResponse.sessionId, createdResponse.workspaceId, wait);
    }
}
