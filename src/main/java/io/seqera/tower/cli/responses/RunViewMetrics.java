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

package io.seqera.tower.cli.responses;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.utils.JsonHelper;

public class RunViewMetrics extends Response {

    public final List<Map<String, Object>> metricsMem;
    public final List<Map<String, Object>> metricsCpu;
    public final List<Map<String, Object>> metricsTime;
    public final List<Map<String, Object>> metricsIo;

    public RunViewMetrics(
            List<Map<String, Object>> metricsMem,
            List<Map<String, Object>> metricsCpu,
            List<Map<String, Object>> metricsTime,
            List<Map<String, Object>> metricsIo
    ) {
        this.metricsMem = metricsMem;
        this.metricsCpu = metricsCpu;
        this.metricsTime = metricsTime;
        this.metricsIo = metricsIo;
    }

    @Override
    public Object getJSON() {
        Map<String, Object> data = new HashMap<>();

        if (!metricsMem.isEmpty()) {
            data.put("metricsMemory", metricsMem);
        }

        if (!metricsCpu.isEmpty()) {
            data.put("metricsCpu", metricsCpu);
        }

        if (!metricsTime.isEmpty()) {
            data.put("metricsTime", metricsTime);
        }

        if (!metricsIo.isEmpty()) {
            data.put("metricsIo", metricsIo);
        }

        return data;
    }

    @Override
    public void toString(PrintWriter out) {
        if (!metricsMem.isEmpty()) {
            try {
                out.println(ansi(String.format("%n    @|bold Memory Metrics|@%n    ----------------------%n%s%n", JsonHelper.prettyJson(metricsMem).replaceAll("(?m)^", "     "))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }


        if (!metricsCpu.isEmpty()) {
            try {
                out.println(ansi(String.format("%n    @|bold   CPU Metrics|@%n    ----------------------%n%s%n", JsonHelper.prettyJson(metricsCpu).replaceAll("(?m)^", "     "))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }


        if (!metricsTime.isEmpty()) {
            try {
                out.println(ansi(String.format("%n    @|bold   Duration Metrics|@%n    ----------------------%n%s%n", JsonHelper.prettyJson(metricsTime).replaceAll("(?m)^", "     "))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }


        if (!metricsIo.isEmpty()) {
            try {
                out.println(ansi(String.format("%n    @|bold  I/O Metrics|@%n    ----------------------%n%s%n", JsonHelper.prettyJson(metricsIo).replaceAll("(?m)^", "     "))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
