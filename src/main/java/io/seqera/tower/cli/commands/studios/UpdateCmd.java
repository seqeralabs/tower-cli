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

import java.util.List;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.labels.Label;
import io.seqera.tower.cli.exceptions.StudioNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.studios.StudioUpdated;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.DataStudioConfiguration;
import io.seqera.tower.model.DataStudioDto;
import io.seqera.tower.model.DataStudioUpdateRequest;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import picocli.CommandLine;

@CommandLine.Command(
        name = "update",
        description = "Update a studio."
)
public class UpdateCmd extends AbstractStudiosCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public StudioRefOptions studioRefOptions;

    @CommandLine.Mixin
    public StudioConfigurationOptions studioConfigOptions;

    @CommandLine.Option(names = {"--labels"}, description = "Comma-separated list of labels", split = ",", converter = Label.StudioResourceLabelsConverter.class)
    public List<Label> labels;

    @CommandLine.Option(names = {"--description"}, description = "Optional configuration override for 'description'.")
    public String description;

    @CommandLine.Option(names = {"--new-name"}, description = "Optional new name for the studio.")
    public String newName;

    @CommandLine.Option(names = {"--ssh"}, description = "Optional override to enable or disable SSH connectivity to the studio.")
    public Boolean ssh;

    @CommandLine.Option(names = {"-c", "--compute-env"}, description = "Move the studio to a different (compatible) compute environment. Only allowed while the studio is stopped.")
    public String computeEnv;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        DataStudioDto studioDto;
        try {
            studioDto = fetchStudio(studioRefOptions, wspId);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new StudioNotFoundException(studioRefOptions.getStudioIdentifier(), workspace.workspace);
            }
            if (e.getCode() == 403) {
                throw new TowerException(String.format("User not entitled to view studio '%s' at %s workspace", studioRefOptions.getStudioIdentifier(), workspace.workspace));
            }
            throw e;
        }

        DataStudioUpdateRequest request = getUpdateRequestWithOverridesApplied(studioDto);

        try {
            DataStudioDto updatedStudio = studiosApi().updateDataStudio(studioDto.getSessionId(), request, wspId);

            return new StudioUpdated(updatedStudio.getSessionId(), studioRefOptions.getStudioIdentifier(), wspId, workspaceRef(wspId));
        } catch (ApiException e) {
            if (computeEnv != null && e.getCode() == 400 && isIncompatibleComputeEnvError(e)) {
                throw incompatibleComputeEnvException(studioDto.getSessionId(), wspId);
            }
            if (e.getCode() == 404) {
                throw new StudioNotFoundException(studioRefOptions.getStudioIdentifier(), workspace.workspace);
            }
            if (e.getCode() == 403) {
                throw new TowerException(String.format("User not entitled to view studio '%s' at %s workspace", studioRefOptions.getStudioIdentifier(), workspace.workspace));
            }
            throw e;
        }
    }

    private boolean isIncompatibleComputeEnvError(ApiException e) {
        String body = e.getResponseBody();
        return body != null && body.contains("is not compatible with the Studio's current compute environment");
    }

    private TowerException incompatibleComputeEnvException(String sessionId, Long wspId) {
        StringBuilder message = new StringBuilder(String.format(
                "Compute environment '%s' is not compatible with studio '%s'.", computeEnv, studioRefOptions.getStudioIdentifier()));
        try {
            List<ListComputeEnvsResponseEntry> compatible = studiosApi()
                    .listDataStudioCompatibleComputeEnvs(sessionId, wspId, null)
                    .getComputeEnvs();
            if (compatible == null || compatible.isEmpty()) {
                message.append(" No compatible compute environments are available in this workspace.");
            } else {
                message.append(" Choose one of the following compatible compute environments:");
                compatible.forEach(ce -> message.append(String.format("%n  - %s (%s)", ce.getName(), ce.getId())));
            }
        } catch (ApiException ex) {
            message.append(" (unable to retrieve the list of compatible compute environments)");
        }
        return new TowerException(message.toString());
    }

    private DataStudioUpdateRequest getUpdateRequestWithOverridesApplied(DataStudioDto studioDto) throws ApiException {
        DataStudioConfiguration newConfig = studioConfigurationFrom(studioDto.getWorkspaceId(), studioDto, studioConfigOptions);
        if (ssh != null) {
            newConfig.setSshEnabled(ssh);
        }
        String appliedDescription = description == null
                ? studioDto.getDescription()
                : description;

        DataStudioUpdateRequest request = new DataStudioUpdateRequest();

        request.setConfiguration(newConfig);
        request.setDescription(appliedDescription);
        request.setLabelIds(getLabelIds(labels, studioDto.getWorkspaceId()));

        if (newName != null) {
            request.setName(newName);
        }

        if (computeEnv != null) {
            ComputeEnvResponseDto ceResponse = computeEnvByRef(studioDto.getWorkspaceId(), computeEnv);
            request.setComputeEnvId(ceResponse.getId());
        }

        return request;
    }

}
