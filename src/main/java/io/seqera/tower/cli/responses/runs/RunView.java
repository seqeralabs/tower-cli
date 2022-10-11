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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FormatHelper;
import io.seqera.tower.cli.utils.JsonHelper;
import io.seqera.tower.cli.utils.TableList;
import io.seqera.tower.model.WorkflowStatus;

import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowId;
import static io.seqera.tower.cli.utils.FormatHelper.formatWorkflowStatus;

public class RunView extends Response {

    public final String workspaceRef;
    public final Map<String, Object> general;
    public final List<String> configFiles;
    public final String configText;
    public final Map<String, Object> params;
    public final String command;
    public final Map<String, Object> status;
    public final List<Map<String, Object>> processes;
    public final Map<String, Object> stats;
    public final Map<String, Object> load;
    public final Map<String, Object> utilization;

    @JsonIgnore
    private final String baseWorkspaceUrl;

    public RunView(
            String workspaceRef,
            Map<String, Object> general,
            List<String> configFiles,
            String configText,
            Map<String, Object> params,
            String command,
            Map<String, Object> status,
            List<Map<String, Object>> processes,
            Map<String, Object> stats,
            Map<String, Object> load,
            Map<String, Object> utilization,
            String baseWorkspaceUrl
    ) {
        this.workspaceRef = workspaceRef;
        this.general = general;
        this.configFiles = configFiles;
        this.configText = configText;
        this.params = params;
        this.command = command;
        this.status = status;
        this.processes = processes;
        this.stats = stats;
        this.load = load;
        this.utilization = utilization;
        this.baseWorkspaceUrl = baseWorkspaceUrl;
    }

    @Override
    public Object getJSON() {
        Map<String, Object> data = new HashMap<>();

        data.put("general", general);

        if (!configFiles.isEmpty()) {
            data.put("configuration_files", configFiles);
        }

        if (configText != null) {
            data.put("configuration_text", configText);
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

        return data;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Run at %s workspace:|@%n", workspaceRef)));

        out.println(ansi(String.format("%n    @|bold General|@")));

        TableList table = new TableList(out, 2);
        table.setPrefix("    ");
        table.addRow("ID", formatWorkflowId(general.get("id").toString(), baseWorkspaceUrl));
        table.addRow("Operation ID", general.get("operationId") != null ? general.get("operationId").toString() : "-");
        table.addRow("Run name", general.get("runName").toString());
        table.addRow("Status", formatWorkflowStatus((WorkflowStatus) general.get("status")));
        table.addRow("Starting date", general.get("startingDate") != null ? FormatHelper.formatTime((OffsetDateTime) general.get("startingDate")) : "No date reported");
        table.addRow("Commit ID", general.get("commitId") != null ? general.get("commitId").toString() : "No commit ID reported");
        table.addRow("Session ID", general.get("sessionId").toString());
        table.addRow("Username", general.get("username").toString());
        table.addRow("Workdir", general.get("workdir").toString());
        table.addRow("Container", general.get("container") != null ? general.get("container").toString() : "No container was reported");
        table.addRow("Executors", general.get("executors") != null ? general.get("executors").toString() : "No executors were reported");
        table.addRow("Compute Environment", general.get("computeEnv").toString());
        table.addRow("Nextflow Version", general.get("nextflowVersion") != null ? general.get("nextflowVersion").toString() : "No Nextflow version reported");
        table.print();

        if (!configFiles.isEmpty()) {
            out.println(ansi(String.format("%n    @|bold Configuration Files|@")));
            TableList tableConfigFiles = new TableList(out, 1);
            tableConfigFiles.setPrefix("    ");
            configFiles.forEach(tableConfigFiles::addRow);
            tableConfigFiles.print();
        }

        if (configText != null) {
            out.println(ansi(String.format("%n    @|bold Resolved Configuration|@%n    ----------------------%n%s%n", configText.replaceAll("(?m)^", "     "))));
        }

        if (!params.isEmpty()) {
            try {
                out.println(ansi(String.format("%n    @|bold Parameters|@%n    ----------%n%s%n", JsonHelper.prettyJson(params).replaceAll("(?m)^", "     "))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        if (command != null) {
            out.println(ansi(String.format("%n    @|bold Command|@")));
            out.println(ansi("    -------"));
            out.println(ansi(String.format("     %s", command)));
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
            out.println(ansi(String.format("%n    @|bold Completed processes|@")));
            TableList tableProcess = new TableList(out, 2);
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
            tableStats.addRow("Wall Time", String.format("%.2f minutes", stats.get("wallTime")));
            tableStats.addRow("CPU Time", String.format("%.2f hours", stats.get("cpuTime")));
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
    }
}
