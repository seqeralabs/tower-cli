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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetUrl;
import io.seqera.tower.model.DatasetDto;
import io.seqera.tower.model.DatasetVersionDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "url",
        description = "Obtain a dataset url."
)
public class UrlCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public DatasetRefOptions datasetRefOptions;

    @CommandLine.Option(names = {"--dataset-version"}, description = "Dataset version to obtain URL from.")
    public Long version;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        DatasetDto dataset = fetchDescribeDatasetResponse(datasetRefOptions, wspId);

        DatasetVersionDto datasetVersion = fetchDatasetVersion(wspId, dataset.getId(), dataset.getMediaType(), version);

        return new DatasetUrl(datasetVersion.getUrl(), getDatasetRef(datasetRefOptions), workspace.workspace);
    }
}
