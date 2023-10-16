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

package io.seqera.tower.cli.responses.runs.utils;

import io.seqera.tower.cli.utils.FormatHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MetricFormatMapper {

    private static final Map<String, Function<Number, Object>> FORMATTER = new HashMap<>();
    static {
        FORMATTER.put("memVirtual", FormatHelper::formatBits);
        FORMATTER.put("memRaw", FormatHelper::formatBits);
        FORMATTER.put("memUsage", FormatHelper::formatPercentage);
        FORMATTER.put("cpuUsage", FormatHelper::formatPercentage);
        FORMATTER.put("cpuRaw", FormatHelper::formatPercentage);
        FORMATTER.put("timeRaw", FormatHelper::formatDurationMillis);
        FORMATTER.put("timeUsage", FormatHelper::formatPercentage);
        FORMATTER.put("reads", FormatHelper::formatBits);
        FORMATTER.put("writes", FormatHelper::formatBits);
    }

    private static final Map<String, Number> PADDING = new HashMap<>();
    static {
        PADDING.put("memVirtual", 6);
        PADDING.put("memRaw", 6);
        PADDING.put("memUsage", 4);
        PADDING.put("cpuUsage", 5);
        PADDING.put("cpuRaw", 5);
        PADDING.put("timeRaw", 8);
        PADDING.put("timeUsage", 4);
        PADDING.put("reads", 6);
        PADDING.put("writes", 6);
    }

    private MetricFormatMapper() {
    }

    public static Function<Number, Object> getFormatTransformer(String key) {
        return FORMATTER.getOrDefault(key, null);
    }

    public static Number getPadding(String key) {
        return PADDING.getOrDefault(key, null);
    }
}
