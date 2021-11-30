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

package io.seqera.tower.cli.commands.runs.tasks;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.TaskView;
import io.seqera.tower.model.DescribeTaskResponse;
import io.seqera.tower.model.Task;
import picocli.CommandLine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@CommandLine.Command(
        name = "task",
        description = "Display pipeline's run task details."
)
public class TaskCmd extends AbstractRunsCmd {

    @CommandLine.ParentCommand
    public ViewCmd parentCommand;

    @CommandLine.Option(names = {"-t"}, description = "Pipeline's run task identifier.", required = true)
    public Long id;

    @CommandLine.Option(names = {"--execution-time"}, description = "Task execution time data.")
    boolean executionTime;

    @CommandLine.Option(names = {"--resources-requested"}, description = "Task requested resources data.")
    boolean resourcesRequested;

    @CommandLine.Option(names = {"--resources-usage"}, description = "Task resources usage data.")
    boolean resourcesUsage;


    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(parentCommand.workspace.workspace);

        DescribeTaskResponse response = api().describeWorkflowTask(parentCommand.id, id, wspId);
        Task task = response.getTask();

        Map<String, Object> general = TaskCmd.parseGeneralData(task);
        String command = task.getScript() != null ? task.getScript() : null;
        String environment = task.getEnv() != null ? task.getEnv() : null;
        Map<String, Object> times = new HashMap<>();
        Map<String, Object> resources = new HashMap<>();
        Map<String, Object> usage = new HashMap<>();

        if (executionTime) {
            times = TaskCmd.parseExecutionTimeData(task);
        }

        if (resourcesRequested) {
            resources = TaskCmd.parseResourcesRequestedData(task);
        }

        if (resourcesUsage) {
            usage = TaskCmd.parseResourcesUsageData(task);
        }

        return new TaskView(general, command, environment, times, resources, usage);
    }

    public static Map<String, Object> parseGeneralData(Task task) {
        Map<String, Object> map = new HashMap<>();

        map.put("taskId", task.getTaskId() != null ? task.getTaskId() : null);
        map.put("id", task.getId() != null ? task.getId() : null);
        map.put("workDir", task.getWorkdir());
        map.put("status", task.getStatus().getValue());

        return map;
    }

    public static Map<String, Object> parseExecutionTimeData(Task task) {
        Map<String, Object> map = new HashMap<>();

        map.put("submit", task.getSubmit() != null ? task.getSubmit() : null);
        map.put("start", task.getStart() != null ? task.getStart() : null);
        map.put("complete", task.getComplete() != null ? task.getComplete() : null);
        map.put("duration", task.getDuration() != null ? task.getDuration() : null);
        map.put("realtime", task.getRealtime() != null ? task.getRealtime() : null);

        return map;
    }

    public static Map<String, Object> parseResourcesRequestedData(Task task) {
        Map<String, Object> map = new HashMap<>();

        map.put("container", task.getContainer());
        map.put("queue", task.getQueue());
        map.put("cpus", task.getCpus() != null ? task.getCpus().toString() : null);
        map.put("memory", task.getMemory() != null ? task.getMemory() : null);
        map.put("disk", task.getDisk() != null ? task.getDisk() : null);
        map.put("time", task.getTime() != null ? task.getTime() : null);
        map.put("executor", task.getExecutor());
        map.put("machineType", task.getMachineType());
        map.put("cloudZone", task.getCloudZone());
        map.put("priceModel", task.getPriceModel() != null ? task.getPriceModel().getValue() : null);
        map.put("cost", task.getCost() != null ? task.getCost().doubleValue() : 0);

        return map;
    }

    public static Map<String, Object> parseResourcesUsageData(Task task) {
        Map<String, Object> map = new HashMap<>();

        map.put("pcpu", task.getPcpu() != null ? task.getPcpu() : 0);
        map.put("rss", task.getRss() != null ? task.getRss() : null);
        map.put("peakRss", task.getPeakRss() != null ? task.getPeakRss() : null);
        map.put("vmem", task.getVmem() != null ? task.getVmem() : null);
        map.put("peakVmem", task.getPeakVmem() != null ? task.getPeakVmem() : null);
        map.put("rchar", task.getRchar() != null ? task.getRchar() : null);
        map.put("wchar", task.getWchar() != null ? task.getWchar() : null);
        map.put("readBytes", task.getReadBytes() != null ? task.getReadBytes() : null);
        map.put("writeBytes", task.getWriteBytes() != null ? task.getWriteBytes() : null);
        map.put("syscr", task.getSyscr() != null ? task.getSyscr() : null);
        map.put("syscw", task.getSyscw() != null ? task.getSyscw() : null);
        map.put("volCtxt", task.getVolCtxt() != null ? task.getVolCtxt() * 1D : null);
        map.put("invCtxt", task.getInvCtxt() != null ? task.getInvCtxt() * 1D : null);

        return map;
    }
}
