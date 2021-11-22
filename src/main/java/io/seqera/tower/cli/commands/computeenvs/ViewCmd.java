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
import io.seqera.tower.cli.responses.ComputeEnvView;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.DescribeComputeEnvResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "view",
        description = "View compute environment."
)
public class ViewCmd extends AbstractComputeEnvCmd {

    @Option(names = {"-i", "--id"}, description = "Compute environment identifier.", required = true)
    public String id;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        
        try {
            DescribeComputeEnvResponse response = api().describeComputeEnv(id, wspId);
            return new ComputeEnvView(id, workspaceRef(wspId), response.getComputeEnv());
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ComputeEnvNotFoundException(id, workspaceRef(wspId));
            }
            throw e;
        }
    }


}
