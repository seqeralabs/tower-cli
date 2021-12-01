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
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvDeleted;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "delete",
        description = "Delete compute environment."
)
public class DeleteCmd extends AbstractComputeEnvCmd {

    @CommandLine.Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String id;

        if (computeEnvRefOptions.computeEnv.computeEnvId != null) {
            id = computeEnvRefOptions.computeEnv.computeEnvId;
        } else {
            ComputeEnv computeEnv = computeEnvByName(wspId, computeEnvRefOptions.computeEnv.computeEnvName);
            id = computeEnv.getId();
        }

        try {
            api().deleteComputeEnv(id, wspId);
            return new ComputeEnvDeleted(id, workspaceRef(wspId));
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ComputeEnvNotFoundException(id, workspaceRef(wspId));
            }
            throw e;
        }
    }
}
