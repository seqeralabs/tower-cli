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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.computeenvs.ComputeEnvDeleted;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.ComputeEnvStatus;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static io.seqera.tower.cli.utils.ResponseHelper.waitStatus;

@Command(
        name = "delete",
        description = "Delete a compute environment."
)
public class DeleteCmd extends AbstractComputeEnvCmd {

    @CommandLine.Mixin
    public ComputeEnvRefOptions computeEnvRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Option(names = {"--wait"}, description = "Wait until the compute environment is fully deleted.")
    public boolean wait;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        String id;

        if (computeEnvRefOptions.computeEnv.computeEnvId != null) {
            id = computeEnvRefOptions.computeEnv.computeEnvId;
        } else {
            ComputeEnvResponseDto computeEnv = computeEnvByName(wspId, computeEnvRefOptions.computeEnv.computeEnvName);
            id = computeEnv.getId();
        }

        try {
            computeEnvsApi().deleteComputeEnv(id, wspId, null);
            return new ComputeEnvDeleted(id, workspaceRef(wspId), wspId);
        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new ComputeEnvNotFoundException(id, workspaceRef(wspId));
            }
            throw e;
        }
    }

    @Override
    protected Integer onBeforeExit(int exitCode, Response response) {
        if (exitCode != 0 || !wait || response == null) {
            return exitCode;
        }

        ComputeEnvDeleted computeEnv = (ComputeEnvDeleted) response;
        boolean showProgress = app().output != OutputType.json;

        try {
            return waitStatus(
                    app().getOut(),
                    showProgress,
                    ComputeEnvStatus.DELETED,
                    ComputeEnvStatus.values(),
                    () -> checkComputeEnvStatus(computeEnv.id, computeEnv.workspaceId),
                    ComputeEnvStatus.ERRORED, ComputeEnvStatus.INVALID
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return exitCode;
        }
    }

    private ComputeEnvStatus checkComputeEnvStatus(String computeEnvId, Long workspaceId) {
        try {
            return computeEnvsApi().describeComputeEnv(computeEnvId, workspaceId, Collections.emptyList()).getComputeEnv().getStatus();
        } catch (ApiException | NullPointerException e) {
            return null;
        }
    }
}