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
import io.seqera.tower.cli.exceptions.NoComputeEnvironmentException;
import io.seqera.tower.cli.responses.ComputeEnvs.ComputeEnvsPrimaryGet;
import io.seqera.tower.cli.responses.ComputeEnvs.ComputeEnvsPrimarySet;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.DescribeComputeEnvResponse;
import picocli.CommandLine;

@CommandLine.Command(
        name = "set",
        description = "Sets a workspace compute environment as primary."
)
public class SetCmd extends AbstractComputeEnvsPrimaryCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Compute environment identifier.", required = true)
    public String id;

    @Override
    protected Response exec() throws ApiException, IOException {

        DescribeComputeEnvResponse describeComputeEnvResponse = api().describeComputeEnv(id, workspace.workspaceId);

        api().updateComputeEnvPrimary(id, workspace.workspaceId, null);

        return new ComputeEnvsPrimarySet(workspaceRef(workspace.workspaceId), describeComputeEnvResponse.getComputeEnv());
    }
}
