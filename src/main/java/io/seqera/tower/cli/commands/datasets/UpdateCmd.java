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
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetUpdate;
import io.seqera.tower.model.Dataset;
import io.seqera.tower.model.UpdateDatasetRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
        name = "update",
        description = "Update a workspace dataset."
)
public class UpdateCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public DatasetRefOptions datasetRefOptions;

    @CommandLine.Option(names = {"--new-name"}, description = "Dataset new name.")
    public String newName;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Dataset description.")
    public String description;

    @CommandLine.Option(names = {"--header"}, description = "Set first row as a header.")
    public boolean header = false;

    @CommandLine.Option(names = {"-f", "--file"}, description = "Data file to upload.")
    Path fileName = null;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        if (fileName == null && header) {
            throw new TowerException("Please provide a new file version to upload or remove the --header option");
        }

        Long wspId = workspaceId(workspace.workspace);
        Dataset dataset = fetchDescribeDatasetResponse(datasetRefOptions, wspId);
        UpdateDatasetRequest request = new UpdateDatasetRequest();
        request.setName(newName != null ? newName : dataset.getName());
        request.setDescription(description != null ? description : dataset.getDescription());

        api().updateDataset(wspId, dataset.getId(), request);

        if (fileName != null) {
            api().uploadDataset(wspId, dataset.getId(), header, fileName.toFile());
        }

        return new DatasetUpdate(dataset.getName(), workspace.workspace, dataset.getId());
    }
}
