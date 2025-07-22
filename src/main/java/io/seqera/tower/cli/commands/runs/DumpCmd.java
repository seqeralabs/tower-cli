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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunDump;
import io.seqera.tower.cli.shared.WorkflowMetadata;
import io.seqera.tower.cli.utils.JsonHelper;
import io.seqera.tower.cli.utils.SilentPrintWriter;
import io.seqera.tower.cli.utils.TarFileHelper;
import io.seqera.tower.model.DescribeTaskResponse;
import io.seqera.tower.model.DescribeWorkflowLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListTasksResponse;
import io.seqera.tower.model.ServiceInfo;
import io.seqera.tower.model.Task;
import io.seqera.tower.model.TaskStatus;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import io.seqera.tower.model.WorkflowMetrics;
import io.seqera.tower.model.WorkflowQueryAttribute;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Command(
    name = "dump",
    description = "Dump all logs and details of a run into a compressed tarball file for troubleshooting."
)
public class DumpCmd extends AbstractRunsCmd {

    public static final List<String> SUPPORTED_FILE_FORMATS = List.of(".tar.xz", ".tar.gz");

    @Option(names = {"-i", "-id"}, description = "Pipeline run identifier.", required = true)
    public String id;

    @Option(names = {"-o", "--output"}, description = "Output file to store the dump. (supported formats: .tar.xz and .tar.gz)", required = true)
    Path outputFile;

    @Option(names = {"--add-task-logs"}, description = "Add all task stdout, stderr and log files.")
    public boolean addTaskLogs;

    @Option(names = {"--add-fusion-logs"}, description = "Add all Fusion task logs.")
    public boolean addFusionLogs;

    @Option(names = {"--only-failed"}, description = "Dump only failed tasks.")
    public boolean onlyFailed;

    @Option(names = {"--silent"}, description = "Do not show download progress.")
    public boolean silent;

    @Mixin
    public WorkspaceOptionalOptions workspace;

    private PrintWriter progress;

    // cached responses
    private DescribeWorkflowResponse workflowDescription;
    private DescribeWorkflowLaunchResponse workflowLaunch;
    private List<Task> workflowTasks;

    @Override
    protected Response exec() throws ApiException, IOException {

        Long wspId = workspaceId(workspace.workspace);
        progress = silent ? new SilentPrintWriter() : app().getOut();

        String fileName = outputFile.getFileName().toString();
        if (SUPPORTED_FILE_FORMATS.stream().noneMatch(fileName::endsWith)) {
            throw new TowerException("Unknown file format. Only 'tar.xz' and 'tar.gz' formats are supported.");
        }

        try(
            var tar = new TarFileHelper()
                    .withFilepath(outputFile)
                    .buildAppender();
        ) {

            tar.add("service-info.json", collectTowerInfo());
            tar.add("workflow.json", collectWorkflowInfo(wspId));
            tar.add("workflow-metadata.json", collectWorkflowMetadata(wspId));
            tar.add("workflow-load.json", collectWorkflowLoad(wspId));

            var wfLaunch = collectWorkflowLaunch(wspId);
            if (wfLaunch != null) {
                tar.add("workflow-launch.json", wfLaunch);
            } else {
                progress.println(ansi("\t- No data collected, skipping")); // nextflow-run workflows doesn't upload launch
            }

            tar.add("workflow-metrics.json", collectWorkflowMetrics(wspId));
            tar.add("workflow-tasks.json", collectWorkflowTasks(wspId));

            var nfLog = collectNfLog(wspId);
            if (nfLog != null) {
                tar.add("nextflow.log", nfLog);
            } else {
                progress.println(ansi("\t- No data collected, skipping")); // nextflow-run workflows doesn't upload log
            }

            collectWorkflowTaskLogs(tar, wspId); // tasks/{taskId}/.command.[out,err,log], .fusion.log

        } // blocks until data is written to tar file, or timeout

        return new RunDump(id, workspaceRef(wspId), outputFile);
    }

    private String collectTowerInfo() throws IOException, ApiException {
        progress.println(ansi("- Tower info"));

        ServiceInfo serviceInfo = serviceInfoApi().info().getServiceInfo();
        return JsonHelper.prettyJson(serviceInfo);
    }

    private String collectWorkflowInfo(Long workspaceId) throws IOException, ApiException {
        progress.println(ansi("- Workflow general information"));

        // General workflow info (including labels)

        Workflow workflow = getWorkflowDescription(workspaceId).getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }

        return JsonHelper.prettyJson(workflow);
    }

    private String collectWorkflowMetadata(Long workspaceId) throws IOException, ApiException {
        progress.println(ansi("- Workflow metadata"));

        // Workflow metadata aggregates including:
        // + labels
        // + optimization status

        var workflowDesc = getWorkflowDescription(workspaceId);
        var workflowLaunchDesc = getWorkflowLaunchDescription(workspaceId);

        var workflow = workflowDesc.getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }

        Long pipelineId = null;
        if (workflowLaunchDesc != null && workflowLaunchDesc.getLaunch() != null) {
            pipelineId = workflowLaunchDesc.getLaunch().getPipelineId();
        }

        WorkflowMetadata wfMetadata = new WorkflowMetadata(
                pipelineId,
                workflowDesc.getOrgId(),
                workflowDesc.getOrgName(),
                workspaceId,
                workflowDesc.getWorkspaceName(),
                workflow.getOwnerId(),
                generateUrl(workspaceId, workflow.getUserName(), workflow.getId()),
                workflowDesc.getLabels()
        );

        return JsonHelper.prettyJson(wfMetadata);
    }

    private String collectWorkflowLoad(Long workspaceId) throws ApiException, JsonProcessingException {
        progress.println(ansi("- Workflow load data"));

        WorkflowLoad workflowLoad = workflowLoadByWorkflowId(workspaceId, id);

        return JsonHelper.prettyJson(workflowLoad);
    }

    private String collectWorkflowLaunch(Long workspaceId) throws ApiException, JsonProcessingException {
        progress.println(ansi("- Workflow launch"));

        var workflow = getWorkflowDescription(workspaceId).getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }

        String launchId = workflow.getLaunchId();
        if (launchId == null) { // nextflow-run workflow, no launch entity available
            return null;
        }
        Launch launch = launchById(workspaceId, launchId);
        return JsonHelper.prettyJson(launch);
    }

    private String collectWorkflowMetrics(Long workspaceId) throws ApiException, JsonProcessingException {
        progress.println(ansi("- Workflow metrics"));

        var workflow = getWorkflowDescription(workspaceId).getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }

        List<WorkflowMetrics> metrics = workflowsApi().describeWorkflowMetrics(workflow.getId(), workspaceId).getMetrics();

        return JsonHelper.prettyJson(metrics);
    }

    private String collectWorkflowTasks(Long workspaceId) throws ApiException, JsonProcessingException {
        progress.println(ansi("- Task details"));

        List<Task> tasks = getWorkflowTasks(workspaceId, id);

        return JsonHelper.prettyJson(tasks);
    }

    private void collectWorkflowTaskLogs(TarFileHelper.TarFileAppender tar, Long workspaceId) throws ApiException, IOException {

        if (!addTaskLogs && !addFusionLogs) {
            return;
        }

        progress.println(ansi("- Task logs"));

        var workflow = getWorkflowDescription(workspaceId).getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }

        String workflowId = id;

        List<Task> tasks = getWorkflowTasks(workspaceId, id);

        if (onlyFailed) {
            tasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.FAILED).collect(Collectors.toList());
        }

        int current = 1;
        int total = tasks.size();

        for (Task task : tasks) {

            progress.println(ansi(String.format("     [%d/%d] adding task logs '%s'", current++, total, task.getName())));

            if (addTaskLogs) {
                addTaskLog(tar, task.getTaskId(), ".command.out", workspaceId, workflowId);
                addTaskLog(tar, task.getTaskId(), ".command.err", workspaceId, workflowId);
                addTaskLog(tar, task.getTaskId(), ".command.log", workspaceId, workflowId);
            }

            if (addFusionLogs) {
                addTaskLog(tar, task.getTaskId(), ".fusion.log", workspaceId, workflowId);
            }
        }
    }

    private File collectNfLog(Long workspaceId) throws ApiException {
        progress.println(ansi("- Workflow nextflow.log"));

        var workflow = getWorkflowDescription(workspaceId).getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }

        if (workflow.getLaunchId() == null) { // nextflow-run workflow, no log available
            return null;
        }
        File nextflowLog = workflowsApi().downloadWorkflowLog(workflow.getId(), String.format("nf-%s.log", workflow.getId()), workspaceId);
        return nextflowLog;
    }

    private void addTaskLog(TarFileHelper.TarFileAppender tar, Long taskId, String logName, Long workspaceId, String workflowId) throws ApiException, IOException {
        try {

            File file = workflowsApi().downloadWorkflowTaskLog(workflowId, taskId, logName, workspaceId);
            String fileName = String.format("tasks/%d/%s", taskId, logName);
            tar.add(fileName, file);

        } catch (ApiException e) {
            // Ignore error 404 that means that the file is no longer available
            // Ignore error 400 that means that the run was launch using Nextflow CLI
            if (e.getCode() != 404 && e.getCode() != 400) {
                throw e;
            }
        }
    }

    private DescribeWorkflowResponse getWorkflowDescription(Long workspaceId) throws ApiException {
        if (this.workflowDescription == null) {
            this.workflowDescription = workflowById(workspaceId, id, List.of(WorkflowQueryAttribute.LABELS, WorkflowQueryAttribute.OPTIMIZED));
        }
        return this.workflowDescription;
    }

    private DescribeWorkflowLaunchResponse getWorkflowLaunchDescription(Long workspaceId) throws ApiException {
        if (this.workflowLaunch == null) {
            var workflow = getWorkflowDescription(workspaceId).getWorkflow();
            if (workflow == null) {
                throw new TowerException("Unknown workflow");
            }
            if (workflow.getLaunchId() != null) {
                this.workflowLaunch = workflowLaunchById(workspaceId, workflow.getId());
            }
        }
        return this.workflowLaunch;
    }

    private List<Task> getWorkflowTasks(Long wspId, String workflowId) throws ApiException {

        if (this.workflowTasks != null) return this.workflowTasks;

        int max = 100;
        int offset = 0;
        int added = max;

        this.workflowTasks = new ArrayList<>();

        while (added == max) {

            added = 0;
            ListTasksResponse response = workflowsApi().listWorkflowTasks(workflowId, wspId, max, offset, null, null, null);

            if (response.getTasks() == null) {
                throw new TowerException("No tasks found for workflow");
            }

            for (DescribeTaskResponse describeTaskResponse : response.getTasks()) {

                Task task = describeTaskResponse.getTask();
                this.workflowTasks.add(task);

                added++;
            }

            offset += max;
        }

        return this.workflowTasks;
    }

    private String generateUrl(Long wspId, String userName, String wfId) throws ApiException {
        if (wspId == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName, wfId);
        }
        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(wspId), workspaceName(wspId), wfId);
    }


}
