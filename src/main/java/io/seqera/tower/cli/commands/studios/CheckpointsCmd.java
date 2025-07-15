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

package io.seqera.tower.cli.commands.studios;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.exceptions.WorkspaceNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.studios.StudioCheckpointsList;
import io.seqera.tower.cli.utils.PaginationInfo;
import io.seqera.tower.model.DataStudioListCheckpointsResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "checkpoints",
        description = "List checkpoints for a studio."
)
public class CheckpointsCmd extends AbstractStudiosCmd {

    private static final Integer STUDIO_LIST_MAX_ALLOWED_CHECKPOINTS = 50;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public StudioRefOptions studioRefOptions;

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Optional filter criteria, allowing free text search on name " +
            "and keywords: `after: YYYY-MM-DD`, `before: YYYY-MM-DD` and `author`. Example keyword usage: -f author:my-name.")
    public String filter;

    @CommandLine.Mixin
    PaginationOptions paginationOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        Integer max = PaginationOptions.getMax(paginationOptions) > STUDIO_LIST_MAX_ALLOWED_CHECKPOINTS
                ? STUDIO_LIST_MAX_ALLOWED_CHECKPOINTS
                : PaginationOptions.getMax(paginationOptions);
        Integer offset = PaginationOptions.getOffset(paginationOptions, max);

        String sessionId = getSessionId(studioRefOptions, wspId);

        DataStudioListCheckpointsResponse response;

        try {
           response = studiosApi().listDataStudioCheckpoints(sessionId, wspId, filter, max, offset);
        } catch (ApiException e) {
            if (e.getCode() == 404){
                throw new WorkspaceNotFoundException(wspId);
            }
            if (e.getCode() == 403) {
                throw new TowerException(String.format("User not entitled to %s workspace", wspId));
            }
            throw e;
        }

        return new StudioCheckpointsList(studioRefOptions.getStudioIdentifier() ,workspaceRef(wspId), response.getCheckpoints(), PaginationInfo.from(paginationOptions, response.getTotalSize()));
    }
}
