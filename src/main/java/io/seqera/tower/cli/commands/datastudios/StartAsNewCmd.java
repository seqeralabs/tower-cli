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

package io.seqera.tower.cli.commands.datastudios;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.InvalidDataStudioParentCheckpointException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datastudios.DataStudiosCreated;
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
        name = "start-as-new",
        description = "Start a new data studio from an existing one"
)
public class StartAsNewCmd extends AbstractStudiosCmd{

    @CommandLine.Mixin
    public ParentDataStudioRefOptions parentDataStudioRefOptions;

    @CommandLine.Option(names = {"--parent-checkpoint-id"}, description = "Parent Data Studio checkpoint id, to be used as staring point for the new Data Studio. If not provided, if will defaults to latest existing checkpoint of parent Data Studio.")
    public String parentCheckpointId;

    @CommandLine.Option(names = {"-n", "--name"}, description = "Data Studio name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Data studio description.")
    public String description;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataStudioConfigurationOptions dataStudioConfigOptions;

    @CommandLine.Option(names = {"-a", "--autoStart"}, description = "Create Data Studio and start it immediately, defaults to false", defaultValue = "false")
    public Boolean autoStart;

    @CommandLine.Option(names = {"--wait"}, description = "Wait until DataStudio is in RUNNING status. Valid options: ${COMPLETION-CANDIDATES}.")
    public DataStudioStatus wait;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        try {
            String parentStudioSessionId = getParentDataStudioSessionId(parentDataStudioRefOptions, wspId);
            DataStudioDto parentDataStudio = api().describeDataStudio(parentStudioSessionId, wspId);
            if (parentDataStudio == null) {
                throw new TowerException(String.format("Parent DataStudio %s not found at %s workspace", parentStudioSessionId, wspId));
            }

            DataStudioCreateRequest request = prepareRequest(parentDataStudio, parentCheckpointId, wspId);
            DataStudioCreateResponse response = api().createDataStudio(request, wspId, autoStart);
            DataStudioDto dataStudioDto = response.getStudio();
            assert dataStudioDto != null;
            return new DataStudiosCreated(dataStudioDto.getSessionId(), wspId, workspaceRef(wspId), baseWorkspaceUrl(wspId), autoStart);
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

        if (parentCheckpointId == null) {
            DataStudioListCheckpointsResponse response = api().listDataStudioCheckpoints(parentDataStudio.getSessionId(), parentDataStudio.getWorkspaceId(), null, 1, null);
            if (!response.getCheckpoints().isEmpty()) {
                request.setInitialCheckpointId(response.getCheckpoints().get(0).getId());
            }
        } else {
            try {
                Long checkpoint = Long.valueOf(parentCheckpointId);
                DataStudioCheckpointDto response = api().getDataStudioCheckpoint(parentDataStudio.getSessionId(), checkpoint, wspId);
                request.setInitialCheckpointId(response.getId());
            } catch (NumberFormatException | ApiException e) {
                throw new InvalidDataStudioParentCheckpointException(parentCheckpointId);
            }
        }

        request.setDataStudioToolUrl(Objects.requireNonNull(parentDataStudio.getTemplate()).getRepository());
        request.setComputeEnvId(Objects.requireNonNull(parentDataStudio.getComputeEnv()).getId());

        DataStudioConfiguration newConfig = dataStudioConfigurationFrom(workspaceId(workspace.workspace), parentDataStudio, dataStudioConfigOptions);

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

        DataStudiosCreated createdResponse = ((DataStudiosCreated) response);
        return onBeforeExit(exitCode, createdResponse.sessionId, createdResponse.workspaceId, wait);
    }
}
