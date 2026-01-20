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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvView;
import io.seqera.tower.model.ComputeEnvResponseDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "view",
        description = "View compute environment."
)
public class ViewCmd extends AbstractComputeEnvCmd {

    @CommandLine.Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);

        ComputeEnvResponseDto computeEnv = fetchComputeEnv(computeEnvRefOptions, wspId);
        return new ComputeEnvView(computeEnv.getId(), workspaceRef(wspId), computeEnv, baseWorkspaceUrl(wspId));
    }

}
