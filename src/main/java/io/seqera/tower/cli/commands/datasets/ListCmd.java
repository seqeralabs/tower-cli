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

package io.seqera.tower.cli.commands.datasets;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetList;
import io.seqera.tower.model.Dataset;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;

@CommandLine.Command(
        name = "list",
        description = "List all workspace datasets."
)
public class ListCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Show only datasets which name contains the given word.")
    public String filter;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        List<Dataset> response = searchByName(wspId, filter);

        return new DatasetList(response, workspace.workspace);
    }
}
