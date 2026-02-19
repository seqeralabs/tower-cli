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

package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.versions.VersionRefOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesView;
import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineQueryAttribute;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "view",
        description = "View pipeline details"
)
public class ViewCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    // Explicit "0..1" for clarity — contrasts with the required "1" in VersionRefOptions. @Mixin won't work here as it would lose mutual exclusivity.
    @CommandLine.ArgGroup(multiplicity = "0..1")
    public VersionRefOptions.VersionRef versionRef;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId, PipelineQueryAttribute.labels);
        Long sourceWorkspaceId = sourceWorkspaceId(wspId, pipeline);
        String versionId = resolvePipelineVersionId(pipeline.getPipelineId(), wspId, versionRef);
        LaunchDbDto launch = pipelinesApi().describePipelineLaunch(pipeline.getPipelineId(), wspId, sourceWorkspaceId, versionId).getLaunch();
        return new PipelinesView(workspaceRef(wspId), pipeline, launch, baseWorkspaceUrl(wspId));
    }
}
