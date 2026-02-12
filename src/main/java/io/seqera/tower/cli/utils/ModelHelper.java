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

package io.seqera.tower.cli.utils;

import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ModelHelper {

    private ModelHelper() {
    }

    public static WorkflowLaunchRequest createLaunchRequest(LaunchDbDto launch) {
        return new WorkflowLaunchRequest()
                .id(launch.getId())
                .computeEnvId(launch.getComputeEnv() != null ? launch.getComputeEnv().getId() : null)
                .pipeline(launch.getPipeline())
                .workDir(launch.getWorkDir())
                .revision(launch.getRevision())
                .sessionId(launch.getSessionId())
                .configProfiles(launch.getConfigProfiles())
                .userSecrets(launch.getUserSecrets())
                .workspaceSecrets(launch.getWorkspaceSecrets())
                .configText(launch.getConfigText())
                .towerConfig(launch.getTowerConfig())
                .paramsText(launch.getParamsText())
                .preRunScript(launch.getPreRunScript())
                .postRunScript(launch.getPostRunScript())
                .mainScript(launch.getMainScript())
                .entryName(launch.getEntryName())
                .schemaName(launch.getSchemaName())
                .pipelineSchemaId(launch.getPipelineSchemaId())
                .resume(launch.getResume())
                .pullLatest(launch.getPullLatest())
                .stubRun(launch.getStubRun())
                .optimizationId(launch.getOptimizationId())
                .optimizationTargets(launch.getOptimizationTargets())
                .headJobCpus(launch.getHeadJobCpus())
                .headJobMemoryMb(launch.getHeadJobMemoryMb())
                .dateCreated(launch.getDateCreated());
    }

    public static <T> T coalesce(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static String coalesce(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    public static List<String> removeEmptyValues(List<String> values) {
        if (values == null) {
            return null;
        }

        return values.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
