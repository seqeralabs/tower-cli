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

import io.seqera.tower.ApiException;
import io.seqera.tower.api.PipelineVersionsApi;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of the frontend version naming algorithm.
 *
 * Generates incremental version names by stripping a trailing -{number} suffix,
 * incrementing it (or starting at 1), and validating with the server.
 */
public class VersionNameHelper {

    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 150;
    private static final Pattern VERSION_SUFFIX = Pattern.compile("-(\\d+)$");

    private VersionNameHelper() {
    }

    /**
     * Generates the next version name from a base name.
     * Strips trailing -{number}, increments (or starts at 1).
     * E.g., "pipeline-3" → "pipeline-4", "rnaseq" → "rnaseq-1"
     */
    public static String generateNextVersionName(String baseName) {
        if (baseName == null || baseName.isEmpty()) {
            return baseName;
        }

        Matcher matcher = VERSION_SUFFIX.matcher(baseName);
        if (matcher.find()) {
            int currentVersion = Integer.parseInt(matcher.group(1));
            String prefix = baseName.substring(0, matcher.start());
            return prefix + "-" + (currentVersion + 1);
        }

        return baseName + "-1";
    }

    /**
     * Generates a valid version name using the naming algorithm with server-side validation.
     * Tries up to 20 incremental names, then falls back to a random 4-char suffix.
     */
    public static String generateValidVersionName(
            String baseName,
            Long pipelineId,
            Long wspId,
            PipelineVersionsApi api
    ) throws ApiException {
        if (baseName == null || baseName.isEmpty()) {
            return generateRandomFallback(baseName != null ? baseName : "");
        }

        Matcher matcher = VERSION_SUFFIX.matcher(baseName);
        int startIndex;
        String prefix;

        if (matcher.find()) {
            startIndex = Integer.parseInt(matcher.group(1)) + 1;
            prefix = baseName.substring(0, matcher.start());
        } else {
            startIndex = 1;
            prefix = baseName;
        }

        for (int i = startIndex; i < startIndex + MAX_RETRIES; i++) {
            String candidate = prefix + "-" + i;
            try {
                api.validatePipelineVersionName(pipelineId, candidate, wspId);
                return candidate;
            } catch (ApiException e) {
                // Validation failed (name already taken), try next
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return generateRandomFallback(prefix);
    }

    private static String generateRandomFallback(String prefix) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return prefix + "-" + random;
    }
}
