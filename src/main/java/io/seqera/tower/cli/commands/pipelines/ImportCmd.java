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
import io.seqera.tower.cli.exceptions.PipelineNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesAdded;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static io.seqera.tower.cli.utils.ModelHelper.coalesce;

@CommandLine.Command(
        name = "import",
        description = "Add a pipeline from file content"
)
public class ImportCmd extends AbstractPipelinesCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"-c", "--compute-env"}, description = "Compute environment name (defaults to value defined in JSON compute environment file)")
    public String computeEnv;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the pipeline if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to import", arity = "1")
    Path fileName = null;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        
        CreatePipelineRequest request;

        request = parseJson(FilesHelper.readString(fileName), CreatePipelineRequest.class);
        request.setName(name);

        WorkflowLaunchRequest launch = request.getLaunch();
        String ceRef = computeEnv != null ? computeEnv : launch.getComputeEnvId();
        if (ceRef == null) {
            throw new TowerException("Missing compute environment ID. Provide it using '--compute-env' option or field 'launch.computeEnvId'.");
        }

        ComputeEnvResponseDto ce = computeEnvByRef(wspId, ceRef);

        // Use compute env values by default
        String workDirValue = coalesce(launch.getWorkDir(), ce.getConfig().getWorkDir());
        String preRunScriptValue = coalesce(launch.getPreRunScript(), ce.getConfig().getPreRunScript());
        String postRunScriptValue = coalesce(launch.getPostRunScript(), ce.getConfig().getPostRunScript());

        launch.setComputeEnvId(ce.getId());
        launch.setWorkDir(workDirValue);
        launch.setPreRunScript(preRunScriptValue);
        launch.setPostRunScript(postRunScriptValue);

        if (overwrite) deletePipeline(name, wspId);

        pipelinesApi().createPipeline(request, wspId);

        return new PipelinesAdded(workspaceRef(wspId), name);
    }

    private void deletePipeline(String name, Long wspId) throws ApiException {
        try {
            PipelineDbDto pipe = pipelineByName(wspId, name);
            pipelinesApi().deletePipeline(pipe.getPipelineId(), wspId);
        } catch (PipelineNotFoundException ignored) {}
    }
}
