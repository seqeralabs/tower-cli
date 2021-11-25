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
    public final boolean groupResults;

    public RunViewMetrics(
            List<MetricColumn> columns,
            List<Map<String, Object>> metricsMem,
            List<Map<String, Object>> metricsCpu,
            List<Map<String, Object>> metricsTime,
            List<Map<String, Object>> metricsIo,
            boolean groupResults
    ) {
        this.columns = columns;
        this.metricsMem = metricsMem;
        this.metricsCpu = metricsCpu;
        this.metricsTime = metricsTime;
        this.metricsIo = metricsIo;
        this.groupResults = groupResults;
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
        List<String> fields = columns.stream().map(Enum::name).collect(Collectors.toList());
        List<String> cols = new ArrayList<>();
        cols.add("process");
        if (!groupResults) {
            cols.add("metric");
        }
        cols.addAll(fields);

        if (!metricsMem.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  Memory Metrics|@%n    ----------------%n")));

            if (groupResults) {
                out.println(ansi(String.format("   @|italic   Legend: execution real-time / %% requested time used|@%n")));
                processDataReducedTable(metricsMem, out, cols);
            } else {
                processDataTable(metricsMem, out, cols);
            }
        }


        if (!metricsCpu.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  CPU Metrics|@%n    ----------------%n")));

            if (groupResults) {
                out.println(ansi(String.format("   @|italic   Legend: raw usage / %% allocated|@%n")));
                processDataReducedTable(metricsCpu, out, cols);
            } else {
                processDataTable(metricsCpu, out, cols);
            }
        }


        if (!metricsTime.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  Time Metrics|@%n    ----------------%n")));

            if (groupResults) {
                out.println(ansi(String.format("   @|italic   Legend: reads / writes|@%n")));
                processDataReducedTable(metricsTime, out, cols);
            } else {
                processDataTable(metricsTime, out, cols);
            }
        }


        if (!metricsIo.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  I/O Metrics|@%n    ----------------%n")));

            if (groupResults) {
                out.println(ansi(String.format("   @|italic   Legend: reads / writes|@%n")));
                processDataReducedTable(metricsIo, out, cols);
            } else {
                processDataTable(metricsIo, out, cols);
            }
        }
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

    private void processDataReducedTable(List<Map<String, Object>> metricData, PrintWriter out, List<String> cols) {
        TableList table = new TableList(out, cols.size(), cols.toArray(new String[0])).sortBy(0);
        table.setPrefix("    ");

        metricData.forEach(processDataBlock -> {
            processDataBlock.forEach((process, sectionDataBlock) -> {
                List<String> cells = new ArrayList<>();
                cells.add(process);
                Map<String, List<String>> data = summarizeDataBlocks((Map<String, Map<String, Object>>) sectionDataBlock);
                if (data.size() > 0) {
                    data.values().stream().forEach(it -> {
                        cells.add(String.join(" / ", it));
                    });

                    table.addRow(cells.toArray(new String[0]));
                }
            });
        });

        table.print();
    }

    private Map<String, List<String>> summarizeDataBlocks(Map<String, Map<String, Object>> data) {
        Map<String, List<String>> result = new HashMap<>();

        data.values().stream().forEach(it -> {
            if (it != null) {
                for (Map.Entry<String, Object> entry : it.entrySet()) {
                    if (!result.containsKey(entry.getKey())) {
                        result.put(entry.getKey(), new ArrayList<>());
                    }

                    result.get(entry.getKey()).add(entry.getValue().toString());
                }
            }
        });

        return result;
    }
}
