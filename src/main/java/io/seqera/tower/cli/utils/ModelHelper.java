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

public class ModelHelper {

    private ModelHelper() {
    }

    public static WorkflowLaunchRequest createLaunchRequest(Launch launch) {
        return new WorkflowLaunchRequest()
                .id(launch.getId())
                .computeEnvId(launch.getComputeEnv().getId())
                .pipeline(launch.getPipeline())
                .workDir(launch.getWorkDir())
                .revision(launch.getRevision())
                .sessionId(launch.getSessionId())
                .configProfiles(launch.getConfigProfiles())
                .configText(launch.getConfigText())
                .paramsText(launch.getParamsText())
                .preRunScript(launch.getPreRunScript())
                .postRunScript(launch.getPostRunScript())
                .mainScript(launch.getMainScript())
                .entryName(launch.getEntryName())
                .schemaName(launch.getSchemaName())
                .resume(launch.getResume())
                .pullLatest(launch.getPullLatest())
                .stubRun(launch.getStubRun())
                .dateCreated(launch.getDateCreated());
    }

    public static <T> T coalesce(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
