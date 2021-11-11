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

package io.seqera.tower.cli.commands.runs.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunViewMetrics;
import io.seqera.tower.model.ResourceData;
import io.seqera.tower.model.WorkflowMetrics;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(
        name = "metrics",
        description = "Display pipeline's run metrics"
)
public class MetricCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Filters by process name")
    public String filter = "";

    @CommandLine.Option(names = {"-t", "--type"}, split = ",", description = "Process metric types separated by comma: 'cpu','mem','time','reads','writes' (default is 'cpu','mem','time'). Use * for all.")
    public List<MetricType> type;

    @CommandLine.Option(names = {"-c", "--columns"}, split = ",", description = "Process metric columns to display: 'mean','min','q1','q2','q3','max' (default is 'mean','min','q1','q2','q3','max'). Use * for all.")
    public List<MetricColumn> columns;

    @CommandLine.ParentCommand
    public ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException {
        type = type == null ? List.of(MetricType.cpu, MetricType.mem, MetricType.time, MetricType.io) : type;
        columns = columns == null ? List.of(MetricColumn.mean, MetricColumn.min, MetricColumn.q1, MetricColumn.q2, MetricColumn.q3, MetricColumn.max) : columns;

        List<WorkflowMetrics> metrics = api().describeWorkflowMetrics(parentCommand.id, parentCommand.workspace.workspaceId).getMetrics();

        List<Map<String, Object>> metricsMem = new ArrayList<>();
        if (type.contains(MetricType.mem)) {
            metrics.forEach(it -> {
                Map<String, Object> data = new HashMap<>();
                if (it.getProcess().contains(filter)) {
                    data.put("memRaw", it.getMem() != null ? processColumns(it.getMem()) : null);
                    data.put("memUsage", it.getMemUsage() != null ? processColumns(it.getMemUsage()) : null);
                    data.put("memVirtual", it.getVmem() != null ? processColumns(it.getVmem()) : null);

                    Map<String, Object> process = new HashMap<>();
                    process.put(it.getProcess(), data);
                    metricsMem.add(process);
                }

            });
        }

        List<Map<String, Object>> metricsCpu = new ArrayList<>();
        if (type.contains(MetricType.cpu)) {
            metrics.forEach(it -> {
                Map<String, Object> data = new HashMap<>();
                if (it.getProcess().contains(filter)) {
                    data.put("cpuRaq", it.getCpu() != null ? processColumns(it.getCpu()) : null);
                    data.put("cpuUsage", it.getCpuUsage() != null ? processColumns(it.getCpuUsage()) : null);

                    Map<String, Object> process = new HashMap<>();
                    process.put(it.getProcess(), data);
                    metricsCpu.add(process);
                }
            });
        }

        List<Map<String, Object>> metricsTime = new ArrayList<>();
        if (type.contains(MetricType.time)) {
            metrics.forEach(it -> {
                Map<String, Object> data = new HashMap<>();
                if (it.getProcess().contains(filter)) {
                    data.put("timeRaw", it.getTime() != null ? processColumns(it.getTime()) : null);
                    data.put("timeUsage", it.getTimeUsage() != null ? processColumns(it.getTimeUsage()) : null);

                    Map<String, Object> process = new HashMap<>();
                    process.put(it.getProcess(), data);
                    metricsTime.add(process);
                }
            });
        }

        List<Map<String, Object>> metricsIo = new ArrayList<>();
        if (type.contains(MetricType.io)) {
            metrics.forEach(it -> {
                Map<String, Object> data = new HashMap<>();
                if (it.getProcess().contains(filter)) {
                    data.put("writes", it.getWrites() != null ? processColumns(it.getWrites()) : null);
                    data.put("reads", it.getReads() != null ? processColumns(it.getReads()) : null);

                    Map<String, Object> process = new HashMap<>();
                    process.put(it.getProcess(), data);
                    metricsIo.add(process);
                }
            });
        }

        return new RunViewMetrics(columns, metricsMem, metricsCpu, metricsTime, metricsIo);
    }

    private Map<String, Object> processColumns(ResourceData resourceData) {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> objectMap = objectMapper.convertValue(resourceData, Map.class);

        Map<String, Object> data = new HashMap<>();

        columns.forEach(col -> {
            if (objectMap.containsKey(col.name()) && col.name() != null) {
                data.put(col.name(), objectMap.get(col.name()));
            }
        });

        return data;
    }
}
