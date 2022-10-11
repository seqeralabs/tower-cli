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

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.runs.download.DownloadCmd;
import io.seqera.tower.cli.commands.runs.metrics.MetricsCmd;
import io.seqera.tower.cli.commands.runs.tasks.TaskCmd;
import io.seqera.tower.cli.commands.runs.tasks.TasksCmd;
import io.seqera.tower.cli.exceptions.RunNotFoundException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunView;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.ProgressData;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
        name = "view",
        description = "View pipeline's runs.",
        subcommands = {
                DownloadCmd.class,
                MetricsCmd.class,
                TasksCmd.class,
                TaskCmd.class,
        }
)
public class ViewCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-i", "--id"}, description = "Pipeline run identifier.", required = true)
    public String id;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @CommandLine.Mixin
    public RunViewOptions opts;

    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        
        try {
            String workspaceRef = workspaceRef(wspId);
            DescribeWorkflowResponse workflowResponse = workflowById(wspId, id);
            Workflow workflow = workflowResponse.getWorkflow();
            WorkflowLoad workflowLoad = workflowLoadByWorkflowId(wspId, id);

            ComputeEnv computeEnv = workflow.getLaunchId() != null ? launchById(wspId, workflow.getLaunchId()).getComputeEnv() : null;

            ProgressData progress = null;
            if (opts.processes || opts.stats || opts.load || opts.utilization) {
                progress = api().describeWorkflowProgress(id, wspId).getProgress();
            }

            Map<String, Object> general = new LinkedHashMap<>();
            general.put("id", workflow.getId());
            general.put("operationId", workflowResponse.getJobInfo() != null ? workflowResponse.getJobInfo().getOperationId() : null);
            general.put("runName", workflow.getRunName());
            general.put("startingDate", workflow.getStart());
            general.put("commitId", workflow.getCommitId());
            general.put("sessionId", workflow.getSessionId());
            general.put("username", workflow.getUserName());
            general.put("workdir", workflow.getWorkDir());
            general.put("container", workflow.getContainer());
            general.put("executors", workflowLoad.getExecutors() != null ? String.join(", ", workflowLoad.getExecutors()) : null);
            general.put("computeEnv", computeEnv == null ? '-' : computeEnv.getName());
            general.put("nextflowVersion", workflow.getNextflow() != null ? workflow.getNextflow().getVersion() : null);
            general.put("status", workflow.getStatus());

            List<String> configFiles = new ArrayList<>();
            String configText = null;
            if (opts.config) {
                configFiles = workflow.getConfigFiles();
                configText = workflow.getConfigText();
            }

            Map<String, Object> params = new HashMap<String, Object>();
            if (opts.params) {
                params = workflow.getParams();
            }

            String command = null;
            if (opts.command) {
                command = workflow.getCommandLine();
            }

            Map<String, Object> status = new HashMap<>();
            if (opts.status) {
                status.put("pending", workflowLoad.getPending());
                status.put("submitted", workflowLoad.getSubmitted());
                status.put("running", workflowLoad.getRunning());
                status.put("cached", workflowLoad.getCached());
                status.put("succeeded", workflowLoad.getSucceeded());
                status.put("failed", workflowLoad.getFailed());
            }

            final List<Map<String, Object>> processes = new ArrayList<>();
            if (opts.processes) {
                progress.getProcessesProgress().forEach(it -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", it.getProcess());
                    data.put("completedTasks", it.getSucceeded() + it.getCached());
                    data.put("totalTasks", it.getPending() + it.getRunning() + it.getCached() + it.getSubmitted() + it.getSucceeded() + it.getFailed());

                    processes.add(data);
                });
            }

            Map<String, Object> stats = new HashMap<>();
            if (opts.stats) {
                stats.put("wallTime", TimeUnit.MILLISECONDS.toSeconds(workflow.getDuration()) / 60D);
                stats.put("cpuTime", TimeUnit.MILLISECONDS.toMinutes(progress.getWorkflowProgress().getCpuTime()) / 60D);
                stats.put("totalMemory", progress.getWorkflowProgress().getMemoryRss() / 1024 / 1024 / 1024D);
                stats.put("read", progress.getWorkflowProgress().getReadBytes() / 1024 / 1024 / 1024D);
                stats.put("write", progress.getWorkflowProgress().getWriteBytes() / 1024 / 1024 / 1024D);
                stats.put("cost", progress.getWorkflowProgress().getCost());
            }

            Map<String, Object> load = new HashMap<>();
            if (opts.load) {
                load.put("peakCpus", progress.getWorkflowProgress().getPeakCpus());
                load.put("loadCpus", progress.getWorkflowProgress().getLoadCpus());
                load.put("peakTasks", progress.getWorkflowProgress().getPeakTasks());
                load.put("loadTasks", progress.getWorkflowProgress().getLoadTasks());
            }

            Map<String, Object> utilization = new HashMap<>();
            if (opts.utilization) {
                utilization.put("memoryEfficiency", progress.getWorkflowProgress().getMemoryEfficiency());
                utilization.put("cpuEfficiency", progress.getWorkflowProgress().getCpuEfficiency());
            }

            return new RunView(
                    workspaceRef,
                    general,
                    configFiles,
                    configText,
                    params,
                    command,
                    status,
                    processes,
                    stats,
                    load,
                    utilization,
                    baseWorkspaceUrl(wspId)
            );

        } catch (ApiException e) {
            if (e.getCode() == 403) {
                // Customize the forbidden message
                throw new RunNotFoundException(id, workspaceRef(wspId));
            }

            throw e;
        }
    }
}
