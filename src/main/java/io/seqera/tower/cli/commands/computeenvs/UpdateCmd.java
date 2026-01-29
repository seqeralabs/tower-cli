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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvUpdated;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.UpdateComputeEnvRequest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "update",
        description = "Update a compute environment."
)
public class UpdateCmd extends AbstractComputeEnvCmd {

    @Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"--new-name"}, description = "New compute environment name.")
    public String newName;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        if (newName != null) {
            try {
                computeEnvsApi().validateComputeEnvName(wspId, newName);
            } catch (ApiException ex) {
                throw new InvalidResponseException(String.format("Compute environment name '%s' is not valid", newName));
            }
        }
        ComputeEnvResponseDto ce = describeCE(computeEnvRefOptions, wspId);


        UpdateComputeEnvRequest req = new UpdateComputeEnvRequest()
                .name(newName != null ? newName : ce.getName());

        computeEnvsApi().updateComputeEnv(ce.getId(), req, wspId);


        return new ComputeEnvUpdated(workspaceRef(wspId), ce.getName());

    }

    private ComputeEnvResponseDto describeCE(ComputeEnvRefOptions computeEnvRefOptions, Long wspId) throws ApiException {
        return fetchComputeEnv(computeEnvRefOptions, wspId);
    }
}
