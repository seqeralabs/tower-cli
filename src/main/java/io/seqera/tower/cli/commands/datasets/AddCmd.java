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
import io.seqera.tower.cli.exceptions.DatasetNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetCreate;
import io.seqera.tower.model.CreateDatasetRequest;
import io.seqera.tower.model.CreateDatasetResponse;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
        name = "add",
        description = "Create a workspace dataset."
)
public class AddCmd extends AbstractDatasetsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Dataset name.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Dataset description.")
    public String description;

    @CommandLine.Option(names = {"--header"}, description = "Set first row as a header.")
    public boolean header = false;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "Data file to upload.", arity = "1")
    Path fileName = null;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the dataset if it already exists.", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        File dataset = fileName.toFile();
        if (!dataset.exists()) {
            throw new TowerException(String.format("File path '%s' do not exists.", fileName));
        }

        if (dataset.isDirectory()) {
            throw new TowerException(String.format("File path '%s' must be a file, not a directory.", fileName));
        }

        Long wspId = workspaceId(workspace.workspace);
        CreateDatasetRequest request = new CreateDatasetRequest();
        request.setName(name);
        request.setDescription(description);

        if (overwrite) tryDeleteDataset(name, wspId);

        CreateDatasetResponse response = api().createDataset(wspId, request);

        api().uploadDataset(wspId, response.getDataset().getId(), header, fileName.toFile());

        return new DatasetCreate(response.getDataset().getName(), workspace.workspace, response.getDataset().getId());
    }

    private void tryDeleteDataset(String datasetName, Long wspId) throws ApiException {
        try {
            deleteDatasetByName(datasetName, wspId);
        } catch (DatasetNotFoundException ignored){}
    }
}
