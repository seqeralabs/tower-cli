/*
 * Copyright 2023, Seqera.
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

package io.seqera.tower.cli.commands.computeenvs.primary;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.ComputeEnvRefOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvsPrimarySet;
import io.seqera.tower.model.ComputeEnvResponseDto;
import picocli.CommandLine;

import java.io.IOException;

@CommandLine.Command(
        name = "set",
        description = "Sets a workspace compute environment as primary."
)
public class SetCmd extends AbstractComputeEnvsPrimaryCmd {

    @CommandLine.Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        ComputeEnvResponseDto ce = fetchComputeEnv(computeEnvRefOptions, wspId);

        api().updateComputeEnvPrimary(ce.getId(), wspId, null);

        return new ComputeEnvsPrimarySet(workspaceRef(wspId), ce);
    }
}
