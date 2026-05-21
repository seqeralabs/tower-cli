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
import io.seqera.tower.cli.commands.labels.LabelsSubcmdOptions;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(name = "labels", description = "Manage dataset labels")
public class LabelsCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public DatasetRefOptions datasetRefOptions;

    @CommandLine.Mixin
    public LabelsSubcmdOptions labelsSubcmdOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(labelsSubcmdOptions.workspace.workspace);
        String datasetId = fetchDescribeDatasetResponse(datasetRefOptions, wspId).getId();

        DatasetsLabelsManager manager = new DatasetsLabelsManager(labelsApi());
        return manager.execute(wspId, datasetId, labelsSubcmdOptions);
    }
}
