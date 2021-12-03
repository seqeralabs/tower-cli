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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.seqera.tower.cli.utils.FormatHelper;

public class MetricFormatMapper {

    public static Map<String, Function<Number, Object>> getMap() {
        Map<String, Function<Number, Object>> formatter = new HashMap<>();

        formatter.put("memVirtual", FormatHelper::formatBits);
        formatter.put("memRaw", FormatHelper::formatBits);
        formatter.put("memUsage", FormatHelper::formatPercentage);
        formatter.put("cpuUsage", FormatHelper::formatBits);
        formatter.put("cpuRaq", FormatHelper::formatPercentage);
        formatter.put("timeRaw", FormatHelper::formatDurationMillis);
        formatter.put("timeUsage", FormatHelper::formatPercentage);
        formatter.put("reads", FormatHelper::formatBits);
        formatter.put("writes", FormatHelper::formatBits);

        return formatter;
    }
}
