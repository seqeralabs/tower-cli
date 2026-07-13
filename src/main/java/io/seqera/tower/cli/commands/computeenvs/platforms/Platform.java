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

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.CredentialsApi;
import io.seqera.tower.model.ComputeConfig;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;

import java.io.IOException;

public interface Platform {

    PlatformEnum type();

    ComputeConfig computeConfig(Long workspaceId, CredentialsApi credentialsApi) throws ApiException, IOException;

    /**
     * Whether to collect Fusion metrics for this compute environment. This is a
     * compute-environment-level flag (set on {@code ComputeEnvComputeConfig}, not the
     * platform config) and only valid on Fusion-capable platforms. Returns {@code null}
     * when the user did not specify it, letting Platform resolve the default.
     */
    default Boolean fusionMetricsCollectionEnabled() {
        return null;
    }
}
