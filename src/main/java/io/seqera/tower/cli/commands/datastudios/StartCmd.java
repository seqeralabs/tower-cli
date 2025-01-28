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
import io.seqera.tower.cli.exceptions.DataStudioNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datastudios.DataStudioStartSubmitted;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioStartRequest;
import io.seqera.tower.model.DataStudioStartResponse;
import io.seqera.tower.model.DataStudioStatus;
import picocli.CommandLine;

import static io.seqera.tower.cli.utils.ResponseHelper.waitStatus;
import static java.lang.Boolean.FALSE;

@CommandLine.Command(
        name = "start",
        description = "Start a data studio."
)
public class StartCmd extends AbstractStudiosCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public DataStudioRefOptions dataStudioRefOptions;

    @CommandLine.Mixin
    public DataStudioConfigurationOptions dataStudioConfigOptions;

    @CommandLine.Option(names = {"--wait"}, description = "Wait until given status or fail. Valid options: ${COMPLETION-CANDIDATES}.")
    public DataStudioStatus wait;

    @CommandLine.Option(names = {"--description"}, description = "Optional configuration override for 'description'.")
    public String description;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        try {
            DataStudioDto dataStudioDto = fetchDataStudio(dataStudioRefOptions, wspId);

            DataStudioStartRequest request = getStartRequestWithOverridesApplied(dataStudioDto);

            DataStudioStartResponse response = api().startDataStudio(dataStudioDto.getSessionId(), request, wspId);

            return new DataStudioStartSubmitted(dataStudioRefOptions.getDataStudioIdentifier(), wspId, workspaceRef(wspId), baseWorkspaceUrl(wspId), response.getJobSubmitted());
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new DataStudioNotFoundException(dataStudioRefOptions.getDataStudioIdentifier(), workspace.workspace);
            }
            if (e.getCode() == 403) {
                throw new TowerException(String.format("User not entitled to view studio '%s' at %s workspace", dataStudioRefOptions.getDataStudioIdentifier(), workspace.workspace));
            }
            throw e;
        }
    }

    @Override
    protected Integer onBeforeExit(int exitCode, Response response) {

        if (exitCode != 0 || wait == null || response == null) {
            return exitCode;
        }

        DataStudioStartSubmitted submitted = (DataStudioStartSubmitted) response;

        // If response declares job failed to submit, don't wait and exit early.
        if (FALSE.equals(submitted.jobSubmitted)) {
            return exitCode;
        }

        return onBeforeExit(exitCode, submitted.studioIdentifier, submitted.workspaceId, wait);
    }

    private DataStudioStartRequest getStartRequestWithOverridesApplied(DataStudioDto dataStudioDto) {
        DataStudioConfiguration dataStudioConfiguration = dataStudioDto.getConfiguration() == null
                ? new DataStudioConfiguration()
                : dataStudioDto.getConfiguration();

        DataStudioConfiguration newConfig = dataStudioConfigurationFrom(dataStudioDto, dataStudioConfigOptions);
        String appliedDescription = description == null
                ? dataStudioDto.getDescription()
                : description;

        DataStudioStartRequest request = new DataStudioStartRequest();

        request.setConfiguration(newConfig);
        request.setDescription(appliedDescription);

        return request;
    }



}