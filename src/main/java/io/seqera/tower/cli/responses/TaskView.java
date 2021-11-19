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
            out.println(ansi("    ---------"));
            out.println(ansi(String.format("     %s", command.trim().replaceAll("\n", String.format("%n     ")))));
        }

        if (environment != null) {
            out.println(ansi(String.format("%n     @|bold Environment|@")));
            out.println(ansi("    -------------"));
            out.println(ansi(String.format("     %s", environment.trim().replaceAll("\n", String.format("%n     ")))));
        }

        if (!times.isEmpty()) {
            out.println(ansi(String.format("%n%n    @|bold  %s|@%n", "Execution time")));
            TableList tableTime = new TableList(out, 3, "Label", "Value", "Description");
            tableTime.setPrefix("    ");
            tableTime.addRow("submit", FormatHelper.formatTime((OffsetDateTime) times.get("submit")), "Timestamp when the task has been submitted");
            tableTime.addRow("start", FormatHelper.formatTime((OffsetDateTime) times.get("start")), "Timestamp when the task execution has started");
            tableTime.addRow("complete", FormatHelper.formatTime((OffsetDateTime) times.get("complete")), "Timestamp when task execution has completed");
            tableTime.addRow("duration", FormatHelper.formatDurationMillis((Long) times.get("duration")), "Time elapsed to complete since the submission i.e. including scheduling time");
            tableTime.addRow("realtime", FormatHelper.formatDurationMillis((Long) times.get("realtime")), "Task execution time i.e. delta between completion and start timestamp i.e. compute wall-time\n");
            tableTime.print();
        }

        if (!resources.isEmpty()) {
            out.println(ansi(String.format("%n%n     @|bold  %s|@%n", "Resources requested")));
            TableList tableResource = new TableList(out, 3, "Label", "Value", "Description");
            tableResource.addRow("container", resources.get("container") != null ? resources.get("container").toString() : "", "Container image name used to execute the task");
            tableResource.addRow("queue", resources.get("queue") != null ? resources.get("queue").toString() : "", "The queue that the executor attempted to run the process on");
            tableResource.addRow("cpus", resources.get("cpus") != null ? resources.get("cpus").toString() : "", "The cpus number request for the task execution");
            tableResource.addRow("memory", FormatHelper.formatBits((Long) resources.get("memory")), "The memory request for the task execution");
            tableResource.addRow("Disk space for execution", FormatHelper.formatBits((Long) resources.get("disk")), "The disk space request for the task execution");
            tableResource.addRow("time", FormatHelper.formatDurationMillis((Long) resources.get("time")), "The time request for the task execution");
            tableResource.addRow("executor", resources.get("executor") != null ? resources.get("executor").toString() : "", "The Nextflow executor used to carry out this task");
            tableResource.addRow("machineType", resources.get("machineType") != null ? resources.get("machineType").toString() : "", "The virtual machine type used to carry out by this task");
            tableResource.addRow("cloudZone", resources.get("cloudZone") != null ? resources.get("cloudZone").toString() : "", "The cloud zone where the job get executed");
            tableResource.addRow("priceModel", resources.get("priceModel") != null ? resources.get("priceModel").toString() : "", "The price model used to charge the job computation");
            tableResource.addRow("cost", FormatHelper.formatCost((Double) resources.get("cost")), "The estimated cost to compute this task");
            tableResource.setPrefix("    ");
            tableResource.print();
        }

        if (!usage.isEmpty()) {
            out.println(ansi(String.format("%n%n     @|bold  %s|@%n", "Resources usage")));
            TableList tableUsage = new TableList(out, 3, "Label", "Value", "Description");
            tableUsage.addRow("pcpu", FormatHelper.formatPercentage((Double) usage.get("pcpu")), "Percentage of CPU used by the process");
            tableUsage.addRow("rss", FormatHelper.formatBits((Long) usage.get("rss")), "Real memory (resident set) size of the process");
            tableUsage.addRow("peakRss", FormatHelper.formatBits((Long) usage.get("peakRss")), "Peak of real memory");
            tableUsage.addRow("vmem", FormatHelper.formatBits((Long) usage.get("vmem")), "Virtual memory size of the process");
            tableUsage.addRow("peakVmem", FormatHelper.formatBits((Long) usage.get("peakVmem")), "Peak of virtual memory");
            tableUsage.addRow("rchar", FormatHelper.formatBits((Long) usage.get("rchar")), "Number of bytes the process read, using any read-like system call from files, pipes, tty, etc");
            tableUsage.addRow("wchar", FormatHelper.formatBits((Long) usage.get("wchar")), "Number of bytes the process wrote, using any write-like system call");
            tableUsage.addRow("readBytes", FormatHelper.formatBits((Long) usage.get("readBytes")), "Number of bytes the process directly read from disk");
            tableUsage.addRow("writeBytes", FormatHelper.formatBits((Long) usage.get("writeBytes")), "Number of bytes the process originally dirtied in the page-cache (assuming they will go to disk later)");
            tableUsage.addRow("syscr", FormatHelper.formatBits((Long) usage.get("syscr")), "Number of read-like system call invocations that the process performed");
            tableUsage.addRow("syscw", FormatHelper.formatBits((Long) usage.get("syscw")), "Number of write-like system call invocations that the process performed");
            tableUsage.addRow("volCtxts", usage.get("volCtxt") != null ? usage.get("volCtxt").toString() : "", "Number of voluntary context switches");
            tableUsage.addRow("invCtxt", usage.get("invCtxt") != null ? usage.get("invCtxt").toString() : "", "Number of involuntary context switches");

            tableUsage.setPrefix("    ");
            tableUsage.print();
        }

    }
}
