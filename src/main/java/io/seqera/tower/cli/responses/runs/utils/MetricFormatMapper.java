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

    private static MetricFormatMapper instance;
    private final Map<String, Function<Number, Object>> formatter = new HashMap<>();
    private final Map<String, Number> padding = new HashMap<>();

    private MetricFormatMapper() {
    }

    public static MetricFormatMapper getInstance() {
        if (instance == null) {
            instance = new MetricFormatMapper();
        }

        return instance;
    }

    public Function<Number, Object> getFormatTransformer(String key) {
        return formatter().getOrDefault(key, null);
    }

    public Number getPadding(String key) {
        return padding().getOrDefault(key, null);
    }

    private Map<String, Function<Number, Object>> formatter() {
        if (formatter.isEmpty()) {
            formatter.put("memVirtual", FormatHelper::formatBits);
            formatter.put("memRaw", FormatHelper::formatBits);
            formatter.put("memUsage", FormatHelper::formatPercentage);
            formatter.put("cpuUsage", FormatHelper::formatBits);
            formatter.put("cpuRaq", FormatHelper::formatPercentage);
            formatter.put("timeRaw", FormatHelper::formatDurationMillis);
            formatter.put("timeUsage", FormatHelper::formatPercentage);
            formatter.put("reads", FormatHelper::formatBits);
            formatter.put("writes", FormatHelper::formatBits);
        }

        return formatter;
    }

    private Map<String, Number> padding() {
        if (padding.isEmpty()) {
            padding.put("mem", 9);
            padding.put("cpu", 9);
            padding.put("time", 7);
            padding.put("io", 9);
        }

        return padding;
    }
}
