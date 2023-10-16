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

package io.seqera.tower.cli.responses.computeenvs;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnvResponseDto;

import java.util.HashMap;
import java.util.Map;

public class ComputeEnvsPrimarySet extends Response {

    public final String workspaceRef;
    public final ComputeEnvResponseDto computeEnv;

    public ComputeEnvsPrimarySet(String workspaceRef, ComputeEnvResponseDto computeEnv) {
        this.workspaceRef = workspaceRef;
        this.computeEnv = computeEnv;
    }

    @Override
    public Object getJSON() {
        Map<String, String> map = new HashMap<>();

        map.put("id", computeEnv.getId());
        map.put("name", computeEnv.getName());
        map.put("platform", computeEnv.getPlatform().getValue());

        return map;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Primary compute environment for workspace '%s' was set to '%s (%s)'  |@%n", workspaceRef, computeEnv.getName(), computeEnv.getId()));
    }
}
