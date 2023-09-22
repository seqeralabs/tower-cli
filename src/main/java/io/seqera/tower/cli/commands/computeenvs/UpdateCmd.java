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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
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
        description = "Update compute environments."
)
public class UpdateCmd extends AbstractComputeEnvCmd {

    @Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"--new-name"}, description = "Compute environment new name.")
    public String newName;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        if (newName != null) {
            try {
                api().validateComputeEnvName(wspId, newName);
            } catch (ApiException ex) {
                throw new InvalidResponseException(String.format("Compute environment name '%s' is not valid", newName));
            }
        }
        ComputeEnvResponseDto ce = describeCE(computeEnvRefOptions, wspId);


        UpdateComputeEnvRequest req = new UpdateComputeEnvRequest()
                .name(newName != null ? newName : ce.getName());

        api().updateComputeEnv(ce.getId(), req, wspId);


        return new ComputeEnvUpdated(workspaceRef(wspId), ce.getName());

    }

    private ComputeEnvResponseDto describeCE(ComputeEnvRefOptions computeEnvRefOptions, Long wspId) throws ComputeEnvNotFoundException, ApiException {
        try {
            return fetchComputeEnv(computeEnvRefOptions, wspId);

        } catch (ApiException e) {
            if (e.getCode() == 403) {
                String ref = computeEnvRefOptions.computeEnv.computeEnvId != null
                        ? computeEnvRefOptions.computeEnv.computeEnvId
                        : computeEnvRefOptions.computeEnv.computeEnvName;
                // Customize the forbidden message
                throw new ComputeEnvNotFoundException(ref, workspaceRef(wspId));
            }

            throw e;
        }
    }
}
