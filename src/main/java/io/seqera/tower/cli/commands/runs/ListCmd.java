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

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunList;
import io.seqera.tower.model.ListWorkflowsResponse;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "list",
        description = "List all pipeline runs."
)
public class ListCmd extends AbstractRunsCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only pipeline runs with names that start with the given word.")
    public String startsWith;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        ListWorkflowsResponse response = api().listWorkflows(wspId, max, offset, startsWith);
        return new RunList(workspaceRef(wspId), response.getWorkflows(), workflowWatchUrlPrefix(wspId));
    }
}
