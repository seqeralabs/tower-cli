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

package io.seqera.tower.cli.commands.studios;

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.labels.Label;
import io.seqera.tower.cli.commands.labels.LabelsOptionalOptions;
import io.seqera.tower.cli.exceptions.StudioNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.studios.StudioStartSubmitted;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioStartRequest;
import io.seqera.tower.model.DataStudioStartResponse;
import io.seqera.tower.model.DataStudioStatus;
import picocli.CommandLine;

import static java.lang.Boolean.FALSE;

@CommandLine.Command(
        name = "start",
        description = "Start a studio."
)
public class StartCmd extends AbstractStudiosCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public StudioRefOptions studioRefOptions;

    @CommandLine.Mixin
    public StudioConfigurationOptions studioConfigOptions;

    @CommandLine.Option(names = {"--wait"}, description = "Wait until given status or fail. Valid options: ${COMPLETION-CANDIDATES}.")
    public DataStudioStatus wait;

    @CommandLine.Option(names = {"--labels"}, description = "Comma-separated list of labels.", split = ",", converter = Label.StudioResourceLabelsConverter.class)
    public List<Label> labels;

    @CommandLine.Option(names = {"--description"}, description = "Optional configuration override for 'description'.")
    public String description;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        try {
            DataStudioDto studioDto = fetchStudio(studioRefOptions, wspId);

            DataStudioStartRequest request = getStartRequestWithOverridesApplied(studioDto);

            DataStudioStartResponse response = api().startDataStudio(studioDto.getSessionId(), request, wspId);

            return new StudioStartSubmitted(studioDto.getSessionId(), studioRefOptions.getStudioIdentifier(), wspId, workspaceRef(wspId), baseWorkspaceUrl(wspId), response.getJobSubmitted());
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new StudioNotFoundException(studioRefOptions.getStudioIdentifier(), workspace.workspace);
            }
            if (e.getCode() == 403) {
                throw new TowerException(String.format("User not entitled to view studio '%s' at %s workspace", studioRefOptions.getStudioIdentifier(), workspace.workspace));
            }
            throw e;
        }
    }

    @Override
    protected Integer onBeforeExit(int exitCode, Response response) {

        if (exitCode != 0 || wait == null || response == null) {
            return exitCode;
        }

        StudioStartSubmitted submitted = (StudioStartSubmitted) response;

        // If response declares job failed to submit, don't wait and exit early.
        if (FALSE.equals(submitted.jobSubmitted)) {
            return exitCode;
        }

        return onBeforeExit(exitCode, submitted.sessionId, submitted.workspaceId, wait);
    }

    private DataStudioStartRequest getStartRequestWithOverridesApplied(DataStudioDto studioDto) throws ApiException {
        DataStudioConfiguration newConfig = studioConfigurationFrom(studioDto.getWorkspaceId(), studioDto, studioConfigOptions);
        String appliedDescription = description == null
                ? studioDto.getDescription()
                : description;

        DataStudioStartRequest request = new DataStudioStartRequest();

        request.setConfiguration(newConfig);
        request.setDescription(appliedDescription);
        request.setLabelIds(getLabelIds(labels, studioDto.getWorkspaceId()));

        return request;
    }



}