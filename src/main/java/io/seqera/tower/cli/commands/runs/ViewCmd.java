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
import io.seqera.tower.model.ComputeEnvComputeConfig;
import io.seqera.tower.model.DescribeWorkflowLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.ProgressData;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import io.seqera.tower.model.WorkflowQueryAttribute;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.seqera.tower.cli.utils.FormatHelper.formatLabels;

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
            DescribeWorkflowResponse workflowResponse = api().describeWorkflow(id, wspId, List.of(WorkflowQueryAttribute.LABELS));
            
            if (workflowResponse == null) {
                throw new RunNotFoundException(id, workspaceRef(wspId));
            }

            Workflow workflow = workflowResponse.getWorkflow();
            WorkflowLoad workflowLoad = workflowLoadByWorkflowId(wspId, id);

            DescribeWorkflowLaunchResponse wfLaunch = api().describeWorkflowLaunch(workflow.getId(), wspId);
            ComputeEnvComputeConfig computeEnv = wfLaunch.getLaunch() != null ? wfLaunch.getLaunch().getComputeEnv() : null;

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
            general.put("labels", formatLabels(workflowResponse.getLabels()));

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
