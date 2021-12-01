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
import io.seqera.tower.cli.commands.datasets.versions.VersionsCmd;
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetView;
import io.seqera.tower.model.Dataset;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "view",
        description = "View a workspace dataset.",
        subcommands = {
                VersionsCmd.class,
        }
)
public class ViewCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public DatasetRefOptions datasetRefOptions;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        Dataset response = fetchDescribeDatasetResponse(datasetRefOptions, wspId);

        return new DatasetView(response, workspace.workspace);
    }
}
