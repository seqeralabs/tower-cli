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

import io.seqera.tower.cli.utils.TableList;

import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunView extends Response {

    public final String workspaceRef;
    public final Map<String, Object> general;
    public final Map<String, Object> config;
    public final Map<String, Object> params;
    public final String command;
    public final Map<String, Object> status;
    public final List<Map<String, Object>> processes;
    public final Map<String, Object> stats;
    public final Map<String, Object> load;
    public final Map<String, Object> utilization;
    public final List<Map<String, Object>> metricsMem;
    public final List<Map<String, Object>> metricsCpu;
    public final List<Map<String, Object>> metricsTime;
    public final List<Map<String, Object>> metricsIo;

    public RunView(
            String workspaceRef,
            Map<String, Object> general,
            Map<String, Object> config,
            Map<String, Object> params,
            String command,
            Map<String, Object> status,
            List<Map<String, Object>> processes,
            Map<String, Object> stats,
            Map<String, Object> load,
            Map<String, Object> utilization,
            List<Map<String, Object>> metricsMem,
            List<Map<String, Object>> metricsCpu,
            List<Map<String, Object>> metricsTime,
            List<Map<String, Object>> metricsIo
    ) {
        this.workspaceRef = workspaceRef;
        this.general = general;
        this.config = config;
        this.params = params;
        this.command = command;
        this.status = status;
        this.processes = processes;
        this.stats = stats;
        this.load = load;
        this.utilization = utilization;
        this.metricsMem = metricsMem;
        this.metricsCpu = metricsCpu;
        this.metricsTime = metricsTime;
        this.metricsIo = metricsIo;
    }

    @Override
    public Object getJSON() {
        Map<String, Object> data = new HashMap<>();

        data.put("general", general);

        if (!config.isEmpty()) {
            data.put("configuration", config);
        }

        if (!params.isEmpty()) {
            data.put("parameters", params);
        }

        if (command != null) {
            data.put("command", command);
        }

        if (!status.isEmpty()) {
            data.put("status", status);
        }

        if (!processes.isEmpty()) {
            data.put("processes", processes);
        }

        if (!stats.isEmpty()) {
            data.put("stats", stats);
        }

        if (!load.isEmpty()) {
            data.put("load", load);
        }

        if (!utilization.isEmpty()) {
            data.put("utilization", utilization);
        }

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
        out.println(ansi(String.format("%n  @|bold Run at %s workspace:|@%n", workspaceRef)));

        out.println(ansi(String.format("%n    @|bold General|@")));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", general.get("id").toString());
        table.addRow("Run name", general.get("runName").toString());
        table.addRow("Starting date", general.get("startingDate") != null ? formatTime((OffsetDateTime) general.get("startingDate")) : "No date reported");
        table.addRow("Commit ID", general.get("commitId") != null ? general.get("commitId").toString() : "No commit ID reported");
        table.addRow("Session ID", general.get("sessionId").toString());
        table.addRow("Username", general.get("username").toString());
        table.addRow("Workdir", general.get("workdir").toString());
        table.addRow("Container", general.get("container") != null ? general.get("container").toString() : "No container was reported");
        table.addRow("Executors", general.get("executors") != null ? general.get("executors").toString() : "No executors were reported");
        table.addRow("Compute Environment", general.get("computeEnv").toString());
        table.addRow("Nextflow Version", general.get("nextflowVersion") != null ? general.get("nextflowVersion").toString() : "No Nextflow version reported");
        table.print();

        if (!config.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Configuration|@")));
            out.println(ansi(config.toString()));
        }

        if (!params.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Parameters|@")));
            out.println(ansi(params.toString()));
        }

        if (command != null) {
            out.println(ansi(String.format("%n    @|bold Command|@")));
            out.println(ansi(String.format("%n    %s", command)));
        }

        if (!status.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Status|@")));
            TableList tableStatus = new TableList(out, 2);
            tableStatus.setPrefix("    ");
            tableStatus.addRow("Pending", status.get("pending").toString());
            tableStatus.addRow("Submitted", status.get("submitted").toString());
            tableStatus.addRow("Running", status.get("running").toString());
            tableStatus.addRow("Cached", status.get("cached").toString());
            tableStatus.addRow("Succeeded", status.get("succeeded").toString());
            tableStatus.addRow("Failed", status.get("failed").toString());
            tableStatus.print();
        }

        if (!processes.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Processes|@")));
            TableList tableProcess = new TableList(out, 2, "Name", "Completed");
            tableProcess.setPrefix("    ");
            processes.forEach(it -> {
                tableProcess.addRow(it.get("name").toString(), it.get("completedTasks").toString() + "/" + it.get("totalTasks").toString());
            });
            tableProcess.print();
        }

        if (!stats.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Stats|@")));
            TableList tableStats = new TableList(out, 2);
            tableStats.setPrefix("    ");
            tableStats.addRow("Wall Time",  String.format("%.2f minutes",stats.get("wallTime")));
            tableStats.addRow("CPU Time",  String.format("%.2f hours",stats.get("cpuTime")));
            tableStats.addRow("Total Memory", String.format("%.2f GB", stats.get("totalMemory")));
            tableStats.addRow("Read", String.format("%.2f GB", stats.get("read")));
            tableStats.addRow("Write", String.format("%.2f GB", stats.get("write")));
            tableStats.addRow("Estimated Cost", String.format("$%.2f", stats.get("cost")));
            tableStats.print();
        }

        if (!load.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Load|@")));
            TableList tableLoad = new TableList(out, 2);
            tableLoad.setPrefix("    ");
            tableLoad.addRow("Cores", load.get("loadCpus").toString() + "/" + load.get("peakCpus").toString());
            tableLoad.addRow("Tasks", load.get("loadTasks").toString() + "/" + load.get("peakTasks").toString());
            tableLoad.print();
        }

        if (!utilization.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Utilization|@")));
            TableList tableUtilization = new TableList(out, 2);
            tableUtilization.setPrefix("    ");
            tableUtilization.addRow("Memory Efficiency", String.format("%.2f", utilization.get("memoryEfficiency")) + "%");
            tableUtilization.addRow("CPU Efficiency", String.format("%.2f", utilization.get("cpuEfficiency")) + "%");
            tableUtilization.print();
        }

        if (!metricsMem.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Memory Metrics|@")));
            out.println(ansi(String.format("%n    %s", metricsMem)));
        }


        if (!metricsCpu.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold CPU Metrics|@")));
            out.println(ansi(String.format("%n    %s", metricsCpu)));
        }


        if (!metricsTime.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Job Duration Metrics|@")));
            out.println(ansi(String.format("%n    %s", metricsTime)));
        }


        if (!metricsIo.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold I/O Metrics|@")));
            out.println(ansi(String.format("%n    %s", metricsIo)));
        }
    }
}
