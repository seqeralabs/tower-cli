/*
 * Copyright 2021-2023, Seqera.
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
 *
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
