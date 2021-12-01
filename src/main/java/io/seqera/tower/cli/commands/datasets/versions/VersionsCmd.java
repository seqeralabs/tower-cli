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

package io.seqera.tower.cli.commands.datasets.versions;

import java.io.IOException;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.datasets.AbstractDatasetsCmd;
import io.seqera.tower.cli.commands.datasets.ViewCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetVersionsList;
import io.seqera.tower.model.Dataset;
import io.seqera.tower.model.ListDatasetVersionsResponse;
import picocli.CommandLine;

@CommandLine.Command(
        name = "versions",
        description = "Display dataset versions."
)
public class VersionsCmd extends AbstractDatasetsCmd {

    @CommandLine.ParentCommand
    public ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(parentCommand.workspace.workspace);
        Dataset dataset = fetchDescribeDatasetResponse(parentCommand.datasetRefOptions, wspId);
        String datasetRef = parentCommand.datasetRefOptions.dataset.datasetName != null ? parentCommand.datasetRefOptions.dataset.datasetName : parentCommand.datasetRefOptions.dataset.datasetId;

        ListDatasetVersionsResponse response = api().listDatasetVersions(wspId, dataset.getId(), dataset.getMediaType());

        return new DatasetVersionsList(response.getVersions(), datasetRef, parentCommand.workspace.workspace);
    }
}
