/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.utils;

import io.seqera.tower.model.Launch;
import io.seqera.tower.model.WorkflowLaunchRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ModelHelper {

    private ModelHelper() {
    }

    public static WorkflowLaunchRequest createLaunchRequest(Launch launch) {
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

    public static List<String> removeEmptyValues(List<String> values) {
        if (values == null) {
            return null;
        }

        return values.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}
