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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesDeleted;
import io.seqera.tower.model.PipelineDbDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "delete",
        description = "Remove a pipeline"
)
public class DeleteCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        Long id;
        String pipelineRef;

        if (pipelineRefOptions.pipeline.pipelineId != null) {
            id = pipelineRefOptions.pipeline.pipelineId;
            pipelineRef = id.toString();
        } else {
            PipelineDbDto pipe = pipelineByName(wspId, pipelineRefOptions.pipeline.pipelineName);
            id = pipe.getPipelineId();
            pipelineRef = pipe.getName();
        }

        pipelinesApi().deletePipeline(id, wspId);

        return new PipelinesDeleted(pipelineRef, workspaceRef(wspId));
    }
}
