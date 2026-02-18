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
import io.seqera.tower.cli.responses.pipelines.versions.ViewPipelineVersionCmdResponse;
import io.seqera.tower.model.ListPipelineVersionsResponse;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Objects;
import java.util.function.Predicate;

@Command(
        name = "view",
        description = "View pipeline version details"
)
public class ViewCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    WorkspaceOptionalOptions workspaceOptions;

    @CommandLine.ArgGroup(multiplicity = "1")
    public VersionRef versionRef;

    public static class VersionRef {
        @CommandLine.Option(names = {"--version-id"}, description = "Pipeline version identifier")
        public String versionId;

        @CommandLine.Option(names = {"--version-name"}, description = "Pipeline version name")
        public String versionName;
    }

    @Override
    protected Response exec() throws ApiException {

        Long wspId = workspaceId(workspaceOptions.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId);

        if (pipeline == null) {
            throwPipelineNotFoundException(pipelineRefOptions, wspId);
        }

        PipelineVersionFullInfoDto version = findVersionByRef(pipeline.getPipelineId(), wspId, versionRef);

        if (version == null) {
            String ref = versionRef.versionId != null ? versionRef.versionId : versionRef.versionName;
            throw new TowerException(String.format("Pipeline version '%s' not found", ref));
        }

        return new ViewPipelineVersionCmdResponse(workspaceOptions.workspace, pipeline.getPipelineId(), pipeline.getName(), version);
    }

    private PipelineVersionFullInfoDto findVersionByRef(Long pipelineId, Long wspId, VersionRef ref) throws ApiException {
        String search = ref.versionName;
        Boolean isPublished = ref.versionName != null ? true : null;
        Predicate<PipelineVersionFullInfoDto> matcher = ref.versionId != null
                ? v -> ref.versionId.equals(v.getId())
                : v -> ref.versionName.equals(v.getName());

        ListPipelineVersionsResponse response = pipelineVersionsApi()
                .listPipelineVersions(pipelineId, wspId, null, null, search, isPublished);

        if (response.getVersions() == null) {
            throw new TowerException("No versions available for the pipeline, check if Pipeline versioning feature is enabled for the workspace");
        }

        return response.getVersions().stream()
                .map(PipelineDbDto::getVersion)
                .filter(Objects::nonNull)
                .filter(matcher)
                .findFirst()
                .orElse(null);
    }
}
