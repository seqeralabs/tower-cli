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
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.ShowLabelsOption;
import io.seqera.tower.cli.commands.global.WorkspaceRequiredOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datasets.DatasetList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.DatasetQueryAttribute;
import io.seqera.tower.model.ListDatasetsResponse;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(
        name = "list",
        description = "List datasets"
)
public class ListCmd extends AbstractDatasetsCmd {

    @CommandLine.Mixin
    public WorkspaceRequiredOptions workspace;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "\"Optional filter criteria, allowing free text search on name or ID \" +\n" +
            "            \"and keywords: `username`, `label`, `visibility`, `createdAfter`, `createdBefore`, `usedAfter`, `usedBefore`. Example keyword usage: -f label:custom-label.\".")
    public String filter;

    @CommandLine.Option(names = {"--show-hidden"}, description = "Include datasets marked as hidden in the results.", defaultValue = "false")
    public boolean showHidden;

    @CommandLine.Mixin
    ShowLabelsOption showLabelsOption;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        List<DatasetQueryAttribute> attributes = Boolean.TRUE.equals(showLabelsOption.showLabels)
                ? List.of(DatasetQueryAttribute.labels)
                : Collections.emptyList();

        String visibility = showHidden ? "all" : null;

        ListDatasetsResponse response = datasetsApi().listDatasetsV2(
                wspId, max, offset, filter, null, null, visibility, attributes
        );

        return new DatasetList(
                response.getDatasets(),
                workspace.workspace,
                Boolean.TRUE.equals(showLabelsOption.showLabels),
                PaginationInfo.from(paginationOptions, response.getTotalSize())
        );
    }
}
