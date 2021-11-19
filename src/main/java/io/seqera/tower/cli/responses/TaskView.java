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

import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.TableList;

import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class TaskView extends Response {

    public final Map<String, Object> general;
    public final String command;
    public final String environment;
    public final Map<String, Object> times;
    public final Map<String, Object> resources;
    public final Map<String, Object> usage;

    public TaskView(
            Map<String, Object> general,
            String command,
            String environment,
            Map<String, Object> times,
            Map<String, Object> resources,
            Map<String, Object> usage
    ) {
        this.general = general;
        this.command = command;
        this.environment = environment;
        this.times = times;
        this.resources = resources;
        this.usage = usage;
    }

    @Override
    public Object getJSON() {
        Map<String, Object> data = new HashMap<>();

        data.put("general", general);

        if (command != null) {
            data.put("command", command);
        }

        if (environment != null) {
            data.put("environment", environment);
        }

        if (!times.isEmpty()) {
            data.put("times", times);
        }

        if (!resources.isEmpty()) {
            data.put("resources", resources);
        }

        if (!usage.isEmpty()) {
            data.put("usage", usage);
        }

        return data;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n%n    @|bold  %s|@", "General")));
        TableList tableGeneral = new TableList(out, 2);
        tableGeneral.setPrefix("    ");
        tableGeneral.addRow("Task ID", general.get("taskId").toString());
        tableGeneral.addRow("ID", general.get("id").toString());
        tableGeneral.addRow("Work directory", general.get("workDir").toString());
        tableGeneral.addRow("Status", general.get("status").toString());
        tableGeneral.print();

        if (command != null) {
            out.println(ansi(String.format("%n     @|bold Command|@")));
            out.println(ansi("    -------"));
            out.println(ansi(String.format("     %s", command.trim().replaceAll("\n", String.format("%n     ")))));
        }

        if (environment != null) {
            out.println(ansi(String.format("%n     @|bold Environment|@")));
            out.println(ansi("    -----------"));
            out.println(ansi(String.format("     %s", environment.trim().replaceAll("\n", String.format("%n     ")))));
        }

        if (!times.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  %s|@", "Execution time")));
            TableList tableTime = new TableList(out, 2);
            tableTime.setPrefix("    ");
            tableTime.addRow("Date submitted", times.get("submit") != null ? FormatHelper.formatTime((OffsetDateTime) times.get("submit")) : "-");
            tableTime.addRow("Date started", times.get("start") != null ? FormatHelper.formatTime((OffsetDateTime) times.get("start")) : "-");
            tableTime.addRow("Date completed", times.get("complete") != null ? FormatHelper.formatTime((OffsetDateTime) times.get("complete")) : "-");
            tableTime.addRow("Time elapsed since the submission", times.get("duration") != null ? String.format("%.2f minutes", times.get("duration")) : "-");
            tableTime.addRow("Task execution time", times.get("realtime") != null ? String.format("%.2f seconds", times.get("realtime")) : "-");
            tableTime.print();
        }

        if (!resources.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  %s|@", "Resources requested")));
            TableList tableResource = new TableList(out, 2);
            tableResource.addRow("Container image name", resources.get("container") != null ? resources.get("container").toString() : "-");
            tableResource.addRow("Queue to run the process on", resources.get("queue") != null ? resources.get("queue").toString() : "-");
            tableResource.addRow("CPU number for execution", resources.get("cpus") != null ? resources.get("cpus").toString() : "-");
            tableResource.addRow("Memory for execution", resources.get("memory") != null ? String.format("%.2f GB", resources.get("memory")) : "-");
            tableResource.addRow("Disk space for execution", resources.get("disk") != null ? String.format("%.2f GB", resources.get("disk")) : "-");
            tableResource.addRow("Time for task execution", resources.get("time") != null ? String.format("%.2f h", resources.get("time")) : "-");
            tableResource.addRow("Nextflow executor used", resources.get("executor") != null ? resources.get("executor").toString() : "-");
            tableResource.addRow("Virtual machine type", resources.get("machineType") != null ? resources.get("machineType").toString() : "-");
            tableResource.addRow("Cloud zone to execute job", resources.get("cloudZone") != null ? resources.get("cloudZone").toString() : "-");
            tableResource.addRow("Price model used to charge computation", resources.get("priceModel") != null ? resources.get("priceModel").toString() : "-");
            tableResource.addRow("Estimated cost to compute task", resources.get("cost") != null ? String.format("$%s", resources.get("cost")) : "-");
            tableResource.setPrefix("    ");
            tableResource.print();
        }

        if (!usage.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  %s|@", "Resources usage")));
            TableList tableUsage = new TableList(out, 2);
            tableUsage.addRow("Percentage of CPU used", usage.get("pcpu") != null ? String.format("%.2f%%", usage.get("pcpu")) : "-");
            tableUsage.addRow("Real memory size", usage.get("rss") != null ? String.format("%.2f KB", usage.get("rss")) : "-");
            tableUsage.addRow("Peak of real memory", usage.get("peakRss") != null ? String.format("%.2f KB", usage.get("peakRss")) : "-");
            tableUsage.addRow("Virtual memory size", usage.get("vmem") != null ? String.format("%.2f KB", usage.get("vmem")) : "-");
            tableUsage.addRow("Peak of virtual memory", usage.get("peakVmem") != null ? String.format("%.2f KB", usage.get("peakVmem")) : "-");
            tableUsage.addRow("Bytes read by process", usage.get("rchar") != null ? String.format("%.2f KB", usage.get("rchar")) : "-");
            tableUsage.addRow("Bytes process by process", usage.get("wchar") != null ? String.format("%.2f KB", usage.get("wchar")) : "-");
            tableUsage.addRow("Bytes directly read from disk", usage.get("readBytes") != null ? String.format("%.2f KB", usage.get("readBytes")) : "-");
            tableUsage.addRow("Bytes originally dirtied in the page-cache", usage.get("writeBytes") != null ? String.format("%.2f", usage.get("writeBytes")) : "-");
            tableUsage.addRow("Read-like system call invocations", usage.get("syscr") != null ? String.format("%.2f KB", usage.get("syscr")) : "-");
            tableUsage.addRow("Write-like system call invocations", usage.get("syscw") != null ? String.format("%.2f KB", usage.get("syscw")) : "-");
            tableUsage.addRow("Voluntary context switches", usage.get("volCtxt") != null ? String.format("%.0f", usage.get("volCtxt")) : "-");
            tableUsage.addRow("Involuntary context switches", usage.get("invCtxt") != null ? String.format("%.0f", usage.get("invCtxt")) : "-");

            tableUsage.setPrefix("    ");
            tableUsage.print();
        }

    }
}
