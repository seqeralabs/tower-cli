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

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.versions.VersionRefOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesSchema;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineSchemaAttributes;
import io.seqera.tower.model.PipelineSchemaResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;

@Command(
        name = "schema",
        description = "Display the parameter schema of a pipeline"
)
public class SchemaCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    // Explicit "0..1" for clarity — contrasts with the required "1" in VersionRefOptions. @Mixin won't work here as it would lose mutual exclusivity.
    @CommandLine.ArgGroup(multiplicity = "0..1")
    public VersionRefOptions.VersionRef versionRef;

    @Option(names = {"--params"}, description = "Display the parameter values the pipeline launches with (pipeline defaults merged with the launch configuration) instead of the schema.")
    public boolean params;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId);
        String versionId = resolvePipelineVersionId(pipeline.getPipelineId(), wspId, versionRef);
        PipelineSchemaAttributes attribute = params ? PipelineSchemaAttributes.params : PipelineSchemaAttributes.schema;

        // Platform answers 204 when it cannot resolve a schema, which the SDK returns as null.
        PipelineSchemaResponse response = pipelinesApi().describePipelineSchema(
                pipeline.getPipelineId(), wspId, sourceWorkspaceId(wspId, pipeline), List.of(attribute), versionId);
        String content = response == null ? null : params ? response.getParams() : response.getSchema();
        if (content == null) {
            throw new TowerException(String.format("No %s found for pipeline '%s'", attribute, pipeline.getName()));
        }

        return new PipelinesSchema(attribute, content);
    }
}
