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
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "view",
        description = "View pipeline version details"
)
public class ViewCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    WorkspaceOptionalOptions workspaceOptions;

    @CommandLine.Mixin
    VersionRefOptions versionRefOptions;

    @Override
    protected Response exec() throws ApiException {

        Long wspId = workspaceId(workspaceOptions.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId);

        if (pipeline == null) {
            throwPipelineNotFoundException(pipelineRefOptions, wspId);
        }

        PipelineVersionFullInfoDto version = findVersionByRef(pipeline.getPipelineId(), wspId, versionRefOptions.versionRef);

        if (version == null) {
            String ref = versionRefOptions.versionRef.versionId != null ? versionRefOptions.versionRef.versionId : versionRefOptions.versionRef.versionName;
            throw new TowerException(String.format("Pipeline version '%s' not found", ref));
        }

        return new ViewPipelineVersionCmdResponse(workspaceOptions.workspace, pipeline.getPipelineId(), pipeline.getName(), version);
    }
}
