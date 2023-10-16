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
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunCanceled;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "cancel",
        description = "Cancel a pipeline execution."
)
public class CancelCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline run identifier.", required = true)
    public String id;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        
        try {
            api().cancelWorkflow(id, wspId, null);

            return new RunCanceled(id, workspaceRef(wspId));
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                throw new RunNotFoundException(id, workspaceRef(wspId));
            }
            throw e;
        }
    }
}
