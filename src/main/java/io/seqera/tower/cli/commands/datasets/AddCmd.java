/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        description = "Add a dataset"
)
public class AddCmd extends AbstractDatasetsCmd {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Dataset name. Must be unique per workspace. Names consist of alphanumeric, hyphen, and underscore characters.", required = true)
    public String name;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Optional dataset description.")
    public String description;

    @CommandLine.Option(names = {"--header"}, description = "Treat first row as header. Default: false.")
    public boolean header = false;

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "Data file to upload", arity = "1")
    Path fileName = null;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the dataset if it already exists", defaultValue = "false")
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

        CreateDatasetResponse response = datasetsApi().createDataset(wspId, request);

        datasetsApi().uploadDataset(wspId, response.getDataset().getId(), header, fileName.toFile());

        return new DatasetCreate(response.getDataset().getName(), workspace.workspace, response.getDataset().getId());
    }

    private void tryDeleteDataset(String datasetName, Long wspId) throws ApiException {
        try {
            deleteDatasetByName(datasetName, wspId);
        } catch (DatasetNotFoundException ignored){}
    }
}
