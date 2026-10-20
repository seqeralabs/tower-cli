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
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import picocli.CommandLine.Command;

import java.util.Collections;
import java.util.List;

@Command
public abstract class AbstractComputeEnvCmd extends AbstractApiCmd {

    protected AbstractComputeEnvCmd() {
    }
    protected ComputeEnvResponseDto fetchComputeEnv(ComputeEnvRefOptions computeEnvRefOptions, Long wspId) throws ApiException {
        ComputeEnvResponseDto computeEnv;

        if (computeEnvRefOptions.computeEnv.computeEnvId != null) {
            computeEnv = computeEnvById(wspId, computeEnvRefOptions.computeEnv.computeEnvId);
        } else {
            computeEnv = computeEnvByName(wspId, computeEnvRefOptions.computeEnv.computeEnvName);
        }

        return computeEnv;
    }

    protected ComputeEnvResponseDto computeEnvByName(Long workspaceId, String name) throws ApiException {

        // KEPT: no server equivalent (tower-cli:rule-1) - the v2 list filter has no `name` field, so the name is resolved client-side
        List<ListComputeEnvsResponseEntry> computeEnvs = computeEnvsClient().list(workspaceId, null);
        ListComputeEnvsResponseEntry entry = computeEnvs
                .stream()
                .filter(ce -> name.equals(ce.getName()))
                .findFirst()
                .orElseThrow(() -> new ComputeEnvNotFoundException(name, workspaceId));

        return computeEnvsClient().describe(workspaceId, entry.getId(), true);
    }

    private ComputeEnvResponseDto computeEnvById(Long workspaceId, String id) throws ApiException {
        return computeEnvsClient().describe(workspaceId, id, true);
    }
}


