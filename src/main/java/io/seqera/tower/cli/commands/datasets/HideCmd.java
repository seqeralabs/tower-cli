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
import io.seqera.tower.cli.responses.datasets.DatasetsVisibility;
import io.seqera.tower.model.ChangeDatasetVisibilityRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;

@CommandLine.Command(
        name = "hide",
        description = "Hide one or more datasets"
)
public class HideCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public DatasetMultiRefOptions datasetMultiRefOptions;

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        List<String> ids = resolveDatasetIds(datasetMultiRefOptions, wspId);

        ChangeDatasetVisibilityRequest request = new ChangeDatasetVisibilityRequest().datasetIds(ids);
        datasetsApi().hideDatasets(request, wspId);

        return new DatasetsVisibility(ids, workspace.workspace, true);
    }
}
