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

package io.seqera.tower.cli.commands.datastudios;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.ShowLabelsOption;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.AbstractPipelinesCmd;
import io.seqera.tower.cli.commands.pipelines.PipelineVisibility;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.datastudios.DataStudiosList;
import io.seqera.tower.cli.responses.pipelines.PipelinesList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.DataStudioListResponse;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.PipelineQueryAttribute;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.List;

@Command(
        name = "list",
        description = "List workspace data studios."
)
public class ListCmd extends AbstractStudiosCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Optional filter criteria, allowing free text search on name and templateUrl " +
            "and keywords: `userName`, `computeEnvName` and `status`. Example keyword usage: -f status:RUNNING")
    public String filter;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        Integer max = PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        DataStudioListResponse response = new DataStudioListResponse();

        try {
           response = api().listDataStudios(wspId, filter, max, offset);
        } catch (ApiException apiException) {
            if (apiException.getCode() == 404){
                throw new WorkspaceNotFoundException(wspId);
            }
        }

        return new DataStudiosList(workspaceRef(wspId), response.getStudios(), PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }
}
