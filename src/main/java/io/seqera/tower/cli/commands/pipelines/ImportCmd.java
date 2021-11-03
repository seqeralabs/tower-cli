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
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesCreated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.CreatePipelineRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

import static io.seqera.tower.cli.utils.JsonHelper.parseJson;

@CommandLine.Command(
        name = "import",
        description = "Create a workspace pipeline from file content"
)
public class ImportCmd extends AbstractPipelinesCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @CommandLine.Option(names = {"-c", "--compute-env"}, description = "Compute environment name (defaults to json file defined environment)")
    public String computeEnv;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "File name to import", arity = "1")
    Path fileName = null;

    @Override
    protected Response exec() throws ApiException, IOException {
        CreatePipelineRequest request;

        request = parseJson(FilesHelper.readString(fileName), CreatePipelineRequest.class);
        request.setName(name);

        if (computeEnv != null) {
            ComputeEnv ce = findComputeEnvironmentByName(computeEnv, workspaceId());
            request.getLaunch().setComputeEnvId(ce.getId());
        } else {
            try {
                api().describeComputeEnv(request.getLaunch().getComputeEnvId(), workspaceId());
            } catch (ApiException apiException) {
                throw new ComputeEnvNotFoundException(request.getLaunch().getId(), workspaceId());
            }
        }

        api().createPipeline(request, workspaceId());

        return new PipelinesCreated(workspaceRef(), name);
    }
}
