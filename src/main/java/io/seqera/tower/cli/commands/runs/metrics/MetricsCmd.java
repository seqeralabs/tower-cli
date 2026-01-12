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

package io.seqera.tower.cli.commands.runs.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.commands.runs.metrics.enums.MetricColumn;
import io.seqera.tower.cli.commands.runs.metrics.enums.MetricPreviewFormat;
import io.seqera.tower.cli.commands.runs.metrics.enums.MetricType;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunViewMetrics;
import io.seqera.tower.model.ResourceData;
import io.seqera.tower.model.WorkflowMetrics;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CommandLine.Command(
        name = "metrics",
        description = "Display pipeline run metrics"
)
public class MetricsCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-f", "--filter"}, description = "Filter by process name")
    public String filter = "";

    @CommandLine.Option(names = {"-t", "--type"}, split = ",", description = "Process metric types separated by comma: ${COMPLETION-CANDIDATES} (default: displays all)")
    public List<MetricType> type;

    @CommandLine.Option(names = {"-c", "--columns"}, split = ",", description = "Process metric columns to display: ${COMPLETION-CANDIDATES} (default: displays all)")
    public List<MetricColumn> columns;

    @CommandLine.Option(names = {"-v", "--view"}, description = "Metric table view mode: ${COMPLETION-CANDIDATES} (default: condensed)")
    public MetricPreviewFormat view = MetricPreviewFormat.condensed;

    @CommandLine.ParentCommand
    public ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(parentCommand.workspace.workspace);

        type = type == null ? List.of(MetricType.cpu, MetricType.mem, MetricType.time, MetricType.io) : type;
        columns = columns == null ? List.of(MetricColumn.min, MetricColumn.q1, MetricColumn.q2, MetricColumn.q3, MetricColumn.max, MetricColumn.mean) : columns;

        List<WorkflowMetrics> metrics = workflowsApi().describeWorkflowMetrics(parentCommand.id, wspId).getMetrics();

        List<Map<String, Object>> metricsMem = new ArrayList<>();
        if (type.contains(MetricType.mem)) {
            metrics.forEach(it -> {
                Map<String, Object> data = new LinkedHashMap<>();
                if (it.getProcess().contains(filter)) {
                    data.put("memRaw", it.getMem() != null ? processColumns(it.getMem()) : null);
                    data.put("memVirtual", it.getVmem() != null ? processColumns(it.getVmem()) : null);
                    data.put("memUsage", it.getMemUsage() != null ? processColumns(it.getMemUsage()) : null);

                    Map<String, Object> process = new HashMap<>();
                    process.put(it.getProcess(), data);
                    metricsMem.add(process);
                }

            });
        }

        List<Map<String, Object>> metricsCpu = new ArrayList<>();
        if (type.contains(MetricType.cpu)) {
            metrics.forEach(it -> {
                Map<String, Object> data = new LinkedHashMap<>();
                if (it.getProcess().contains(filter)) {
                    data.put("cpuRaw", it.getCpu() != null ? processColumns(it.getCpu()) : null);
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
                Map<String, Object> data = new LinkedHashMap<>();
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
                Map<String, Object> data = new LinkedHashMap<>();
                if (it.getProcess().contains(filter)) {
                    data.put("reads", it.getReads() != null ? processColumns(it.getReads()) : null);
                    data.put("writes", it.getWrites() != null ? processColumns(it.getWrites()) : null);

                    Map<String, Object> process = new HashMap<>();
                    process.put(it.getProcess(), data);
                    metricsIo.add(process);
                }
            });
        }

        return new RunViewMetrics(columns, metricsMem, metricsCpu, metricsTime, metricsIo, view);
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
