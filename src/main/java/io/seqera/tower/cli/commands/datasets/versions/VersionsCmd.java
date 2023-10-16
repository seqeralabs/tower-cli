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

package io.seqera.tower.cli.commands.datasets.versions;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.datasets.AbstractDatasetsCmd;
import io.seqera.tower.cli.commands.datasets.ViewCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetVersionsList;
import io.seqera.tower.model.Dataset;
import io.seqera.tower.model.ListDatasetVersionsResponse;
import picocli.CommandLine;

import java.io.IOException;

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
