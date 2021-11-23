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

import io.seqera.tower.cli.commands.runs.metrics.enums.MetricColumn;
import io.seqera.tower.cli.utils.TableList;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunViewMetrics extends Response {

    public final List<MetricColumn> columns;
    public final List<Map<String, Object>> metricsMem;
    public final List<Map<String, Object>> metricsCpu;
    public final List<Map<String, Object>> metricsTime;
    public final List<Map<String, Object>> metricsIo;

    public RunViewMetrics(
            List<MetricColumn> columns,
            List<Map<String, Object>> metricsMem,
            List<Map<String, Object>> metricsCpu,
            List<Map<String, Object>> metricsTime,
            List<Map<String, Object>> metricsIo
    ) {
        this.columns = columns;
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

    private void processDataTable(List<Map<String, Object>> metricData, PrintWriter out, List<String> cols) {
        TableList table = new TableList(out, cols.size(), cols.toArray(new String[0]));
        table.setPrefix("    ");

        metricData.forEach(processDataBlock -> {
            processDataBlock.forEach((process, sectionDataBlock) -> {
                ((Map<String, Object>) sectionDataBlock).forEach((dataBlockDef, data) -> {
                    List<String> cells = new ArrayList<>();
                    cells.add(process);
                    cells.add(dataBlockDef);

                    if (data != null) {
                        ((Map<String, Object>) data).forEach((k, v) -> {
                            cells.add(v.toString());
                        });
                        table.addRow(cells.toArray(new String[0]));
                    }
                });

            });
        });

        table.print();
    }

    @Override
    public void toString(PrintWriter out) {
        List<String> fields = columns.stream().map(Enum::name).collect(Collectors.toList());
        List<String> cols = new ArrayList<>();
        cols.add("process");
        cols.add("metric");
        cols.addAll(fields);

        if (!metricsMem.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  Memory Metrics|@%n    ----------------%n")));
            processDataTable(metricsMem, out, cols);
        }


        if (!metricsCpu.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  CPU Metrics|@%n    ----------------%n")));
            processDataTable(metricsCpu, out, cols);
        }


        if (!metricsTime.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  Time Metrics|@%n    ----------------%n")));
            processDataTable(metricsTime, out, cols);
        }


        if (!metricsIo.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  I/O Metrics|@%n    ----------------%n")));
            processDataTable(metricsIo, out, cols);
        }
    }
}
