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
import io.seqera.tower.model.LinkVersionRequest;
import io.seqera.tower.model.SourceType;
import io.seqera.tower.model.ValidateUrlRequest;
import io.seqera.tower.model.ValidateUrlResponse;
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

    @CommandLine.Parameters(index = "0", paramLabel = "FILENAME", description = "Data file to upload. Not allowed with --url.", arity = "0..1")
    Path fileName = null;

    @CommandLine.Option(names = {"--url"}, description = "Public HTTP or HTTPS URL of a CSV or TSV file to link instead of uploading a file. Runs read the file from the URL.")
    public String url;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @CommandLine.Option(names = {"--overwrite"}, description = "Overwrite the dataset if it already exists", defaultValue = "false")
    public Boolean overwrite;

    @Override
    protected Response exec() throws ApiException, IOException {
        if ((fileName == null) == (url == null)) {
            throw new TowerException("Provide either a FILENAME to upload or a --url to link");
        }
        if (fileName != null) {
            File dataset = fileName.toFile();
            if (!dataset.exists()) {
                throw new TowerException(String.format("File path '%s' do not exists.", fileName));
            }

            if (dataset.isDirectory()) {
                throw new TowerException(String.format("File path '%s' must be a file, not a directory.", fileName));
            }
        }

        Long wspId = workspaceId(workspace.workspace);
        // Like the UI, check the URL before creating the dataset so that an unusable URL leaves no empty dataset behind.
        if (url != null) {
            ValidateUrlResponse validation = datasetsApi().validateDatasetUrl(new ValidateUrlRequest().url(url), wspId);
            if (!Boolean.TRUE.equals(validation.getValid())) {
                throw new TowerException(String.format("Dataset URL '%s' cannot be linked: %s", url, validation.getErrorMessage()));
            }
        }

        CreateDatasetRequest request = new CreateDatasetRequest();
        request.setName(name);
        request.setDescription(description);
        if (url != null) {
            request.setSourceType(SourceType.LINKED);
        }

        if (overwrite) tryDeleteDataset(name, wspId);

        CreateDatasetResponse response = datasetsApi().createDatasetV2(request, wspId);
        String datasetId = response.getDataset().getId();

        if (url != null) {
            datasetsApi().linkDatasetVersion(datasetId, new LinkVersionRequest().url(url).hasHeader(header), wspId);
        } else {
            datasetsApi().uploadDatasetV2(datasetId, wspId, header, fileName.toFile());
        }

        return new DatasetCreate(response.getDataset().getName(), workspace.workspace, datasetId);
    }

    private void tryDeleteDataset(String datasetName, Long wspId) throws ApiException {
        try {
            deleteDatasetByName(datasetName, wspId);
        } catch (DatasetNotFoundException ignored){}
    }
}
