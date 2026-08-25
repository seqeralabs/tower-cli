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

package io.seqera.tower.cli.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.seqera.tower.JSON;
import io.seqera.tower.model.ComputeEnvComputeConfig;
import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.WorkflowLaunchRequest;
import io.seqera.tower.model.WorkflowLaunchResponse;

import java.util.List;
import java.util.stream.Collectors;

public class ModelHelper {

    /**
     * The SDK mapper ignores unknown properties and understands {@code JsonNullable}, so it can copy a launch
     * configuration field by field without a hand-maintained field list. See {@link #copyLaunchFields}.
     */
    private static final ObjectMapper LAUNCH_MAPPER = new JSON().getContext(WorkflowLaunchRequest.class);

    private ModelHelper() {
    }

    public static WorkflowLaunchRequest createLaunchRequest(LaunchDbDto launch) {
        return copyLaunchFields(launch)
                .computeEnvId(computeEnvId(launch.getComputeEnv()));
    }

    public static WorkflowLaunchRequest createLaunchRequest(WorkflowLaunchResponse launch) {
        return copyLaunchFields(launch)
                .computeEnvId(computeEnvId(launch.getComputeEnv()));
    }

    /**
     * Copies every launch field the source and {@link WorkflowLaunchRequest} have in common by serializing the
     * source and reading it back as a request. Fields the request does not declare are dropped, and fields the
     * API gains in a future SDK release are carried over with no change here.
     * <p>
     * Going through JSON is what makes this safe for the nullable fields ({@code syntaxParser},
     * {@code nextflowVersion}, {@code outputDir}): an undefined value stays undefined instead of becoming an
     * explicit {@code null}, which the fluent setters cannot express because they wrap their argument in
     * {@code JsonNullable.of()} unconditionally.
     */
    private static WorkflowLaunchRequest copyLaunchFields(Object launch) {
        return LAUNCH_MAPPER.convertValue(launch, WorkflowLaunchRequest.class);
    }

    private static String computeEnvId(ComputeEnvComputeConfig computeEnv) {
        return computeEnv != null ? computeEnv.getId() : null;
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
