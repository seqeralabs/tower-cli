/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesAdded;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;
import static io.seqera.tower.cli.utils.ModelHelper.coalesce;

@CommandLine.Command(
        name = "import",
        description = "Add a workspace pipeline from file content."
)
public class ImportCmd extends AbstractPipelinesCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name.", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"-c", "--compute-env"}, description = "Compute environment name [default: as defined in json environment file].")
    public String computeEnv;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to import.", arity = "1")
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

        ComputeEnv ce = computeEnvByRef(wspId, ceRef);

        // Use compute env values by default
        String workDirValue = coalesce(launch.getWorkDir(), ce.getConfig().getWorkDir());
        String preRunScriptValue = coalesce(launch.getPreRunScript(), ce.getConfig().getPreRunScript());
        String postRunScriptValue = coalesce(launch.getPostRunScript(), ce.getConfig().getPostRunScript());

        launch.setComputeEnvId(ce.getId());
        launch.setWorkDir(workDirValue);
        launch.setPreRunScript(preRunScriptValue);
        launch.setPostRunScript(postRunScriptValue);

        api().createPipeline(request, wspId);

        return new PipelinesAdded(workspaceRef(wspId), name);
    }
}
