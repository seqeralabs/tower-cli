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

package io.seqera.tower.cli.commands.workspaces.settings.studios;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.workspaces.AbstractWorkspaceCmd;
import io.seqera.tower.cli.commands.workspaces.WorkspaceRefOptions;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.workspaces.StudiosSettingsUpdated;
import io.seqera.tower.model.DataStudioWorkspaceSettingsRequest;
import io.seqera.tower.model.DataStudioWorkspaceSettingsRequest.NameStrategyEnum;
import io.seqera.tower.model.DataStudioWorkspaceSettingsResponse;
import io.seqera.tower.model.OrgAndWorkspaceDto;
import io.seqera.tower.model.UpdateWorkspaceRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "update",
        description = "Update the Studios settings of a workspace. Only the provided options are changed; the rest are left untouched."
)
public class UpdateCmd extends AbstractWorkspaceCmd {

    @CommandLine.Mixin
    public WorkspaceRefOptions workspaceRefOptions;

    @Option(names = {"--container-repository"}, description = "Default container repository used to store Studios container images built or augmented with Wave.")
    public String containerRepository;

    @Option(names = {"--reset-container-repository"}, description = "Clear the default container repository.")
    public boolean resetContainerRepository;

    @Option(names = {"--name-strategy"}, converter = NameStrategyConverter.class, description = "Wave strategy used to name Studios container images. Valid values: none, tagPrefix, imageSuffix.")
    public NameStrategyEnum nameStrategy;

    @Option(names = {"--reset-name-strategy"}, description = "Clear the container image naming strategy.")
    public boolean resetNameStrategy;

    @Option(names = {"--lifespan-hours"}, description = "Maximum lifespan, in hours, of a Studio session before it is automatically stopped. Set to 0 for unlimited lifespan.")
    public Integer lifespanHours;

    @Option(names = {"--private-by-default"}, negatable = true, description = "Whether new Studios are private by default (use --no-private-by-default to disable).")
    public Boolean privateStudioByDefault;

    @Override
    protected Response exec() throws ApiException, IOException {

        boolean updates = containerRepository != null
                || resetContainerRepository
                || nameStrategy != null
                || resetNameStrategy
                || lifespanHours != null
                || privateStudioByDefault != null;

        if (!updates) {
            throw new ShowUsageException(getSpec(), "Required at least one option to update");
        }

        if (containerRepository != null && resetContainerRepository) {
            throw new ShowUsageException(getSpec(), "Options '--container-repository' and '--reset-container-repository' are mutually exclusive");
        }

        if (nameStrategy != null && resetNameStrategy) {
            throw new ShowUsageException(getSpec(), "Options '--name-strategy' and '--reset-name-strategy' are mutually exclusive");
        }

        OrgAndWorkspaceDto ws = fetchOrgAndWorkspaceDbDto(workspaceRefOptions);

        // Fetch the current settings and seed the request so unmodified attributes are preserved.
        DataStudioWorkspaceSettingsResponse current = workspacesApi()
                .findDataStudiosWorkspaceSettings(ws.getOrgId(), ws.getWorkspaceId(), new UpdateWorkspaceRequest());

        DataStudioWorkspaceSettingsRequest request = new DataStudioWorkspaceSettingsRequest()
                .containerRepository(current.getContainerRepository())
                .lifespanHours(current.getLifespanHours())
                .nameStrategy(current.getNameStrategy() == null ? null : NameStrategyEnum.fromValue(current.getNameStrategy()))
                .privateStudioByDefault(current.getPrivateStudioByDefault());

        if (containerRepository != null) {
            request.setContainerRepository(containerRepository);
        } else if (resetContainerRepository) {
            request.setContainerRepository(null);
        }

        if (nameStrategy != null) {
            request.setNameStrategy(nameStrategy);
        } else if (resetNameStrategy) {
            request.setNameStrategy(null);
        }

        if (lifespanHours != null) {
            request.setLifespanHours(lifespanHours);
        }

        if (privateStudioByDefault != null) {
            request.setPrivateStudioByDefault(privateStudioByDefault);
        }

        workspacesApi().updateDataStudiosWorkspaceSettings(ws.getOrgId(), ws.getWorkspaceId(), request);

        return new StudiosSettingsUpdated(ws.getWorkspaceName());
    }

    public static class NameStrategyConverter implements CommandLine.ITypeConverter<NameStrategyEnum> {
        @Override
        public NameStrategyEnum convert(String value) {
            try {
                return NameStrategyEnum.fromValue(value);
            } catch (IllegalArgumentException e) {
                return NameStrategyEnum.valueOf(value.toUpperCase());
            }
        }
    }
}
