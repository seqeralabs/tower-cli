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

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.ShowLabelsOption;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.ListWorkflowsResponse;
import io.seqera.tower.model.WorkflowQueryAttribute;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(
        name = "list",
        description = "List pipeline runs"
)
public class ListCmd extends AbstractRunsCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Filter pipeline runs by run name. Performs case-insensitive substring matching on the runName field.")
    public String filter;

    @CommandLine.Mixin
    ShowLabelsOption showLabelsOption;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        List queryAttribute = Collections.emptyList();
        if(showLabelsOption.showLabels) {
             queryAttribute = List.of(WorkflowQueryAttribute.labels);
        }

        ListWorkflowsResponse response = workflowsApi().listWorkflows(queryAttribute, wspId, max, offset, filter);
        return new RunList(workspaceRef(wspId), response.getWorkflows(), baseWorkspaceUrl(wspId), showLabelsOption.showLabels, PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }


}
