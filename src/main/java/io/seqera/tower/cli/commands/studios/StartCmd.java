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
import io.seqera.tower.cli.commands.labels.LabelsOptionalOptions;
import io.seqera.tower.cli.exceptions.StudioNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.studios.StudioSsh;
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

    @CommandLine.Option(names = {"--labels"}, description = "Comma-separated list of labels", split = ",", converter = Label.StudioResourceLabelsConverter.class)
    public List<Label> labels;

    @CommandLine.Option(names = {"--description"}, description = "Optional configuration override for 'description'.")
    public String description;

    @CommandLine.Option(names = {"--spot"}, description = "Optional override to launch the studio on spot instances.")
    public Boolean spot;

    @CommandLine.Option(names = {"--ssh"}, arity = "0..1", fallbackValue = "true", paramLabel = "true|false", description = "Enable or disable SSH access to the Studio. Bare --ssh or --ssh=true enables it, --ssh=false disables it. Omit to keep the Studio's current setting. Requires SSH to be enabled for the workspace; otherwise Platform ignores it.")
    public Boolean ssh;

    @CommandLine.Option(names = {"--allow-user"}, description = "Override the user (numeric ID, username, or email), besides the creator, allowed to connect to and start this studio when it is private. Omit to leave the allow list unchanged. Only the studio creator may change it.")
    public String allowedUser;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        try {
            DataStudioDto studioDto = fetchStudio(studioRefOptions, wspId);

            DataStudioStartRequest request = getStartRequestWithOverridesApplied(studioDto);

            DataStudioStartResponse response = studiosApi().startDataStudio(studioDto.getSessionId(), request, wspId);

            // The studio was fetched before the start request, so its SSH details are stale if --ssh changes the setting.
            String sshCommand = ssh == null || ssh ? StudioSsh.commandOf(studioDto) : null;
            return new StudioStartSubmitted(studioDto.getSessionId(), studioRefOptions.getStudioIdentifier(), wspId, workspaceRef(wspId), baseWorkspaceUrl(wspId), response.getJobSubmitted(), sshCommand);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new StudioNotFoundException(studioRefOptions.getStudioIdentifier(), workspace.workspace);
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

        return onBeforeExit(exitCode, submitted.sessionId, submitted.workspaceId, wait, submitted.sshCommand == null && !FALSE.equals(ssh));
    }

    private DataStudioStartRequest getStartRequestWithOverridesApplied(DataStudioDto studioDto) throws ApiException {
        DataStudioConfiguration newConfig = studioConfigurationFrom(studioDto.getWorkspaceId(), studioDto, studioConfigOptions);
        if (ssh != null) {
            newConfig.setSshEnabled(ssh);
        }
        String appliedDescription = description == null
                ? studioDto.getDescription()
                : description;

        DataStudioStartRequest request = new DataStudioStartRequest();

        request.setConfiguration(newConfig);
        request.setDescription(appliedDescription);
        request.setLabelIds(getLabelIds(labels, studioDto.getWorkspaceId()));
        request.setAllowedUserIds(resolveAllowedUserIds(allowedUser, studioDto.getWorkspaceId()));
        if (spot != null) {
            request.setSpot(spot);
        }

        return request;
    }



}