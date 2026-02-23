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

package io.seqera.tower.cli.commands.pipelines;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesExport;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.cli.utils.ModelHelper;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

@CommandLine.Command(
        name = "export",
        description = "Export a pipeline"
)
public class ExportCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to export", arity = "0..1")
    String fileName = null;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId);
        Long sourceWorkspaceId = sourceWorkspaceId(wspId, pipeline);
        LaunchDbDto launch = pipelinesApi().describePipelineLaunch(pipeline.getPipelineId(), wspId, sourceWorkspaceId, null).getLaunch();

        WorkflowLaunchRequest workflowLaunchRequest = ModelHelper.createLaunchRequest(launch);

        CreatePipelineRequest createPipelineRequest = new CreatePipelineRequest();
        createPipelineRequest.setDescription(pipeline.getDescription());
        createPipelineRequest.setIcon(pipeline.getIcon());
        createPipelineRequest.setLaunch(workflowLaunchRequest);

        String configOutput = "";

        try {
            configOutput = new JSON().getContext(CreatePipelineRequest.class).writerWithDefaultPrettyPrinter().writeValueAsString(createPipelineRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (fileName != null && !fileName.equals("-")) {
            FilesHelper.saveString(fileName, configOutput);
        }

        return new PipelinesExport(configOutput, fileName);
    }
}
