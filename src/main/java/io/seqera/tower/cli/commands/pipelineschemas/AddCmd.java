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

package io.seqera.tower.cli.commands.pipelineschemas;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelineschemas.PipelineSchemasAdded;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.CreatePipelineSchemaRequest;
import io.seqera.tower.model.CreatePipelineSchemaResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

@Command(
        name = "add",
        description = "Add a pipeline schema"
)
public class AddCmd extends AbstractPipelineSchemasCmd {

    @Option(names = {"-c", "--content"}, description = "Path to a file containing the pipeline schema content.", required = true)
    public Path content;

    @Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        String schemaContent = FilesHelper.readString(content);

        CreatePipelineSchemaResponse response = pipelineSchemasApi().createPipelineSchema(
                new CreatePipelineSchemaRequest()
                        .content(schemaContent),
                wspId
        );

        return new PipelineSchemasAdded(workspaceRef(wspId), response.getPipelineSchema().getId());
    }
}
