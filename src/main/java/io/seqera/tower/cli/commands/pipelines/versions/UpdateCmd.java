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

package io.seqera.tower.cli.commands.pipelines.versions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.AbstractPipelinesCmd;
import io.seqera.tower.cli.commands.pipelines.PipelineRefOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.versions.UpdatePipelineVersionCmdResponse;
import io.seqera.tower.cli.utils.ResponseHelper;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import io.seqera.tower.model.PipelineVersionManageRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "update",
        description = "Update a pipeline version name or default flag"
)
public class UpdateCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    WorkspaceOptionalOptions workspaceOptions;

    @CommandLine.Mixin
    VersionRefOptions versionRefOptions;

    @CommandLine.ArgGroup(exclusive = false, multiplicity = "1",
            heading = "%nUpdate options (at least one required):%n")
    public UpdateOptions updateOptions;

    public static class UpdateOptions {
        @CommandLine.Option(names = {"--new-name"}, description = "New name for the pipeline version")
        public String name;

        @CommandLine.Option(names = {"--set-default"}, description = "Set this version as the default")
        public Boolean isDefault;
    }

    @Override
    protected Response exec() throws ApiException {

        Long wspId = workspaceId(workspaceOptions.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId);

        if (pipeline == null) {
            throwPipelineNotFoundException(pipelineRefOptions, wspId);
        }

        String resolvedVersionId = resolveVersionId(pipeline.getPipelineId(), wspId);

        PipelineVersionManageRequest request = new PipelineVersionManageRequest()
                .name(updateOptions.name)
                .isDefault(updateOptions.isDefault);

        try {
            pipelineVersionsApi().managePipelineVersion(
                    pipeline.getPipelineId(),
                    resolvedVersionId,
                    request,
                    wspId
            );
        } catch (ApiException e) {
            if (e.getCode() == 400) {
                throw new TowerException(String.format("Invalid version name '%s': %s", updateOptions.name, ResponseHelper.decodeMessage(e)));
            }
            throw new TowerException(
                    String.format("Unable to update pipeline version '%s': %s", resolvedVersionId, ResponseHelper.decodeMessage(e))
            );
        }

        return new UpdatePipelineVersionCmdResponse(workspaceOptions.workspace, pipeline.getPipelineId(), pipeline.getName(), resolvedVersionId);
    }

    private String resolveVersionId(Long pipelineId, Long wspId) throws ApiException {
        if (versionRefOptions.versionRef.versionId != null) {
            return versionRefOptions.versionRef.versionId;
        }

        PipelineVersionFullInfoDto version = findVersionByRef(pipelineId, wspId, versionRefOptions.versionRef);
        if (version == null) {
            throw new TowerException(String.format("Pipeline version '%s' not found", versionRefOptions.versionRef.versionName));
        }
        return version.getId();
    }
}
