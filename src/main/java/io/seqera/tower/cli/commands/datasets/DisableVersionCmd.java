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
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetVersionDisabled;
import io.seqera.tower.model.DatasetDto;
import picocli.CommandLine;

@CommandLine.Command(
        name = "disable-version",
        description = "Disable a dataset version so it can no longer be used for new runs. A disabled version cannot be enabled again."
)
public class DisableVersionCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public DatasetRefOptions datasetRefOptions;

    @CommandLine.Option(names = {"--dataset-version"}, description = "Dataset version to disable", required = true)
    public Long version;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        DatasetDto dataset = fetchDescribeDatasetResponse(datasetRefOptions, wspId);

        datasetsApi().disableDatasetVersion(dataset.getId(), version, wspId);

        return new DatasetVersionDisabled(getDatasetRef(datasetRefOptions), version, workspace.workspace);
    }
}
