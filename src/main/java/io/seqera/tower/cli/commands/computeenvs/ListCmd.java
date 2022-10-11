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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvList;
import io.seqera.tower.model.ListComputeEnvsResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "list",
        description = "List all workspace compute environments."
)
public class ListCmd extends AbstractComputeEnvCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        
        ListComputeEnvsResponse response = api().listComputeEnvs(null, wspId);
        return new ComputeEnvList(workspaceRef(wspId), response.getComputeEnvs(), baseWorkspaceUrl(wspId));
    }
}
