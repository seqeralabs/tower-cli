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
