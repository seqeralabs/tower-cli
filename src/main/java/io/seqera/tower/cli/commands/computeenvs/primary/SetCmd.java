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

package io.seqera.tower.cli.commands.computeenvs.primary;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.ComputeEnvRefOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvsPrimarySet;
import io.seqera.tower.model.ComputeEnvResponseDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "set",
        description = "Sets a workspace compute environment as primary."
)
public class SetCmd extends AbstractComputeEnvsPrimaryCmd {

    @CommandLine.Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        ComputeEnvResponseDto ce = fetchComputeEnv(computeEnvRefOptions, wspId);

        api().updateComputeEnvPrimary(ce.getId(), wspId, null);

        return new ComputeEnvsPrimarySet(workspaceRef(wspId), ce);
    }
}
