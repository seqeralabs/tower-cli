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

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.AbstractComputeEnvCmd;
import io.seqera.tower.cli.responses.ComputeEnvs.ComputeEnvsPrimaryGet;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;
import picocli.CommandLine;

@CommandLine.Command(
        name = "get",
        description = "Gets a workspace primary compute environment."
)
public class GetCmd extends AbstractComputeEnvsPrimaryCmd {

    @Override
    protected Response exec() throws ApiException, IOException {
        ComputeEnv primary = primaryComputeEnv(workspace.workspaceId);

        return new ComputeEnvsPrimaryGet(workspaceRef(workspace.workspaceId), primary);
    }
}
