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

package io.seqera.tower.cli.commands.studios;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.studios.StudioLog;
import io.seqera.tower.model.DataStudioLogResponse;
import picocli.CommandLine;

@CommandLine.Command(
        name = "logs",
        description = "Display the process log of the latest studio session."
)
public class LogsCmd extends AbstractStudiosCmd {

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public StudioRefOptions studioRefOptions;

    @CommandLine.Option(names = {"--next"}, description = "Pagination cursor of the log page to display, as printed at the end of the previous page.")
    public String next;

    @CommandLine.Option(names = {"--max-length"}, description = "Maximum number of characters to return.")
    public Long maxLength;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String sessionId = getSessionId(studioRefOptions, wspId);

        DataStudioLogResponse response = studiosApi().getDataStudioLog(sessionId, wspId, next, maxLength);

        return new StudioLog(studioRefOptions.getStudioIdentifier(), workspaceRef(wspId), response.getLog());
    }
}
