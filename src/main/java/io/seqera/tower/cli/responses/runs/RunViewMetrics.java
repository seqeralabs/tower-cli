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

package io.seqera.tower.cli.responses.runs;

import io.seqera.tower.cli.commands.runs.metrics.enums.MetricColumn;
import io.seqera.tower.cli.commands.runs.metrics.enums.MetricPreviewFormat;
import io.seqera.tower.cli.responses.runs.utils.MetricFormatMapper;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.TableList;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

// TODO: Refactor using the new Metric model instead nested maps to make it more readable.
public class RunViewMetrics extends Response {

    public final List<MetricColumn> columns;
    public final List<Map<String, Object>> metricsMem;
    public final List<Map<String, Object>> metricsCpu;
    public final List<Map<String, Object>> metricsTime;
    public final List<Map<String, Object>> metricsIo;
    public final MetricPreviewFormat groupType;

    public RunViewMetrics(
            List<MetricColumn> columns,
            List<Map<String, Object>> metricsMem,
            List<Map<String, Object>> metricsCpu,
            List<Map<String, Object>> metricsTime,
            List<Map<String, Object>> metricsIo,
            MetricPreviewFormat groupType
    ) {
        this.columns = columns;
        this.metricsMem = metricsMem;
        this.metricsCpu = metricsCpu;
        this.metricsTime = metricsTime;
        this.metricsIo = metricsIo;
        this.groupType = groupType;
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
        if (groupType == MetricPreviewFormat.expanded) {
            cols.add("metric");
        }
        cols.addAll(fields);

        if (!metricsMem.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  Memory Metrics|@%n    ----------------%n")));

            if (groupType == MetricPreviewFormat.condensed) {
                out.println(ansi(String.format("   @|italic   Legend:  physical RAM / virtual RAM+swap / %%RAM allocated |@%n")));
                processDataReducedTable(metricsMem, out, cols);
            } else {
                processExpandedDataTable(metricsMem, out, cols);
            }
        }


        if (!metricsCpu.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  CPU Metrics|@%n    ----------------%n")));

            if (groupType == MetricPreviewFormat.condensed) {
                out.println(ansi(String.format("   @|italic   Legend: raw usage / %% allocated|@%n")));
                processDataReducedTable(metricsCpu, out, cols);
            } else {
                processExpandedDataTable(metricsCpu, out, cols);
            }
        }


        if (!metricsTime.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  Time Metrics|@%n    ----------------%n")));

            if (groupType == MetricPreviewFormat.condensed) {
                out.println(ansi(String.format("   @|italic   Legend: execution real-time / %% requested time used|@%n")));
                processDataReducedTable(metricsTime, out, cols);
            } else {
                processExpandedDataTable(metricsTime, out, cols);
            }
        }


        if (!metricsIo.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  I/O Metrics|@%n    ----------------%n")));

            if (groupType == MetricPreviewFormat.condensed) {
                out.println(ansi(String.format("   @|italic   Legend: reads / writes|@%n")));
                processDataReducedTable(metricsIo, out, cols);
            } else {
                processExpandedDataTable(metricsIo, out, cols);
            }
        }
    }

    /**
     * Process data into a regular extended data table.
     *
     * @param metricData
     * @param out
     * @param cols
     */
    private void processExpandedDataTable(List<Map<String, Object>> metricData, PrintWriter out, List<String> cols) {
        TableList table = new TableList(out, cols.size(), cols.toArray(new String[0]));
        table.setPrefix("    ");

        metricData.forEach(processDataBlock -> {
            processDataBlock.forEach((process, sectionDataBlock) -> {
                ((Map<String, Object>) sectionDataBlock).forEach((dataBlockDef, data) -> {
                    List<String> cells = new ArrayList<>();
                    cells.add(process);
                    cells.add(dataBlockDef);

                    Function<Number, Object> fnc = MetricFormatMapper.getInstance().getFormatTransformer(dataBlockDef);

                    // This where data cells are created.
                    if (data != null) {
                        ((Map<String, Number>) data).forEach((k, v) -> {
                            cells.add(fnc.apply(v).toString());
                        });

                        table.addRow(cells.toArray(new String[0]));
                    }
                });

            });
        });

        table.print();
    }

    /**
     * Process data into a summarized or condensed table.
     *
     * @param metricData
     * @param out
     * @param cols
     */
    private void processDataReducedTable(List<Map<String, Object>> metricData, PrintWriter out, List<String> cols) {
        TableList table = new TableList(out, cols.size(), cols.toArray(new String[0])).sortBy(0);
        table.setPrefix("    ");

        metricData.forEach(processDataBlock -> {
            processDataBlock.forEach((process, sectionDataBlock) -> {
                List<String> cells = new ArrayList<>();
                cells.add(process);
                Map<String, List<String>> data = summarizeDataBlocks((Map<String, Map<String, Number>>) sectionDataBlock);
                if (data.size() > 0) {

                    // This where summarized data cells are created into a concatenated string.
                    data.values().stream().forEach(it -> {
                        cells.add(String.join(" ", it));
                    });

                    table.addRow(cells.toArray(new String[0]));
                }
            });
        });

        table.print();
    }

    /**
     * Transform a set of data blocks into a condensed and summarized single data block.
     *
     * @param data
     * @return
     */
    private Map<String, List<String>> summarizeDataBlocks(Map<String, Map<String, Number>> data) {
        Map<String, List<String>> result = new HashMap<>();

        data.entrySet().stream().forEach(it -> {
            if (it.getValue() != null) {
                Function<Number, Object> fnc = MetricFormatMapper.getInstance().getFormatTransformer(it.getKey());

                for (Map.Entry<String, Number> entry : it.getValue().entrySet()) {
                    if (!result.containsKey(entry.getKey())) {
                        result.put(entry.getKey(), new ArrayList<>());
                    }

                    Number padding =  MetricFormatMapper.getInstance().getPadding(it.getKey());
                    result.get(entry.getKey()).add(String.format("%1$"+padding+"s", fnc.apply(entry.getValue())));
                }
            }
        });

        return result;
    }
}
