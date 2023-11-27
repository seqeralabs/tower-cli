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
import io.seqera.tower.cli.utils.SilentPrintWriter;
import io.seqera.tower.model.DescribeTaskResponse;
import io.seqera.tower.model.DescribeWorkflowLaunchResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListParticipantsResponse;
import io.seqera.tower.model.ListTasksResponse;
import io.seqera.tower.model.ParticipantDbDto;
import io.seqera.tower.model.ServiceInfo;
import io.seqera.tower.model.Task;
import io.seqera.tower.model.TaskStatus;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLoad;
import io.seqera.tower.model.WorkflowMetrics;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import picocli.CommandLine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CommandLine.Command(
        name = "dump",
        description = "Dump all logs and details of a run into a compressed tarball file for troubleshooting."
)
public class DumpCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-i", "-id"}, description = "Pipeline run identifier.", required = true)
    public String id;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Output file to store the dump. (supported formats: .tar.xz and .tar.gz)", required = true)
    Path outputFile;

    @CommandLine.Option(names = {"--add-task-logs"}, description = "Add all task stdout, stderr and log files.")
    public boolean addTaskLogs;

    @CommandLine.Option(names = {"--add-fusion-logs"}, description = "Add all Fusion task logs.")
    public boolean addFusionLogs;

    @CommandLine.Option(names = {"--only-failed"}, description = "Dump only failed tasks.")
    public boolean onlyFailed;

    @CommandLine.Option(names = {"--silent"}, description = "Do not show download progress.")
    public boolean silent;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    private final static JSON JSON = new JSON();

    private ExecutorService compressExecutor;
    private static class AddEntry implements Runnable {
        TarArchiveOutputStream out;
        String fileName;
        File file;

        public AddEntry(TarArchiveOutputStream out, String fileName, File file) {
            this.out = out;
            this.fileName = fileName;
            this.file = file;
        }

        @Override
        public void run() {
            TarArchiveEntry entry = new TarArchiveEntry(file, fileName);
            try {
                out.putArchiveEntry(entry);
                IOUtils.copy(new FileInputStream(file), out);
                out.closeArchiveEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Response exec() throws ApiException, IOException {

        Long wspId = workspaceId(workspace.workspace);
        String workflowId = id;
        PrintWriter progress = silent ? new SilentPrintWriter() : app().getOut();
        FileOutputStream fileOut = new FileOutputStream(outputFile.toFile());
        BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);

        String fileName = outputFile.getFileName().toString();
        OutputStream compressOut = compressStream(fileName, buffOut);
        if (compressOut == null) {
            throw new TowerException("Unknown file format. Only 'tar.xz' and 'tar.gz' formats are supported.");
        }

        TarArchiveOutputStream out = new TarArchiveOutputStream(compressOut);
        compressExecutor = Executors.newSingleThreadExecutor();

        dumpTowerInfo(progress, out);
        dumpWorkflowDetails(progress, out, wspId);
        dumpTasks(progress, out, wspId, workflowId);

        compressExecutor.shutdown();
        try {
            if (!compressExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                throw new TowerException("Timeout compressing logs");
            }
        } catch (InterruptedException ignored) {
        }
        out.close();
        return new RunDump(id, workspaceRef(wspId), outputFile);
    }

    private OutputStream compressStream(String fileName, OutputStream out) throws IOException {
        if (fileName.endsWith(".tar.xz")) {
            return new XZCompressorOutputStream(out);
        }

        if (fileName.endsWith(".tar.gz")) {
            return new GzipCompressorOutputStream(out);
        }

        return null;
    }

    private void dumpTasks(PrintWriter progress, TarArchiveOutputStream out, Long wspId, String workflowId) throws ApiException, IOException {
        progress.println(ansi("- Task details"));
        List<Task> tasks = loadTasks(wspId, workflowId);
        addEntry(out, "workflow-tasks.json", List.class, tasks);
        addTaskLogs(progress, out, wspId, workflowId, tasks);
    }

    private void dumpWorkflowDetails(PrintWriter progress, TarArchiveOutputStream out, Long wspId) throws ApiException, IOException {
        progress.println(ansi("- Workflow details"));

        // General workflow info
        DescribeWorkflowResponse workflowResponse = workflowById(wspId, id);
        Workflow workflow = workflowResponse.getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }

        // Launch info
        Launch launch = null;
        Long pipelineId = null;
        if (workflow.getLaunchId() != null) {

            launch = launchById(wspId, workflow.getLaunchId());

            DescribeWorkflowLaunchResponse wfLaunchResponse = workflowLaunchById(wspId, workflow.getId());
            if (wfLaunchResponse != null && wfLaunchResponse.getLaunch() != null) {
                pipelineId = wfLaunchResponse.getLaunch().getPipelineId();
            }
        }

        // User info
        Long userId = workflow.getOwnerId();
        String userMail = null;
        String userName = workflow.getUserName();
        try {
            /**
             * There is no way of obtaining the user email directly, we need to extract it out of the workspace participant list.
             * List the participants of the workspace, find the participant by username, then return that participant email.
             */
            ListParticipantsResponse participants = api().listWorkspaceParticipants(workflowResponse.getOrgId(), workflowResponse.getWorkspaceId(), null, null, null);
            userMail = participants.getParticipants().stream()
                    .filter(participant -> userName.equals(participant.getUserName()))
                    .findFirst()
                    .map(ParticipantDbDto::getEmail)
                    .orElse(null);
        } catch (ApiException ignored) {}

        // Load and metrics info
        WorkflowLoad workflowLoad = workflowLoadByWorkflowId(wspId, id);
        List<WorkflowMetrics> metrics = api().describeWorkflowMetrics(workflow.getId(), wspId).getMetrics();

        WorkflowMetadata wfMetadata = new WorkflowMetadata(
                pipelineId,
                wspId,
                workflowResponse.getWorkspaceName(),
                userId,
                userMail,
                generateUrl(wspId, userName, workflow.getId())
        );

        addEntry(out, "workflow.json", Workflow.class, workflow);
        addEntry(out, "workflow-metadata.json", WorkflowMetadata.class, wfMetadata);
        addEntry(out, "workflow-load.json", WorkflowLoad.class, workflowLoad);
        addEntry(out, "workflow-launch.json", Launch.class, launch);
        addEntry(out, "workflow-metrics.json", List.class, metrics);

        // Files
        try {
            File nextflowLog = api().downloadWorkflowLog(workflow.getId(), String.format("nf-%s.log", workflow.getId()), wspId);
            addFile(out, "nextflow.log", nextflowLog);
        } catch (ApiException e) {
            // Ignore error 404 that means that the file is no longer available
            // Ignore error 400 that means that the run was launch using Nextflow CLI
            if (e.getCode() != 404 && e.getCode() != 400) {
                throw e;
            }
        }
    }

    private void dumpTowerInfo(PrintWriter progress, TarArchiveOutputStream out) throws IOException, ApiException {
        progress.println(ansi("- Tower info"));
        addEntry(out, "service-info.json", ServiceInfo.class, api().info().getServiceInfo());
    }

    private void addTaskLogs(PrintWriter progress, TarArchiveOutputStream out, Long wspId, String workflowId, List<Task> tasks) throws IOException, ApiException {
        if (!addTaskLogs && !addFusionLogs) {
            return;
        }

        if (onlyFailed) {
            tasks = tasks.stream().filter(t -> t.getStatus() == TaskStatus.FAILED).collect(Collectors.toList());
        }

        int current = 1;
        int total = tasks.size();
        for (Task task : tasks) {
            progress.println(ansi(String.format("     [%d/%d] adding task logs '%s'", current++, total, task.getName())));

            if (addTaskLogs) {
                try {
                    File taskOut = api().downloadWorkflowTaskLog(workflowId, task.getTaskId(), ".command.out", wspId);
                    addFile(out, String.format("tasks/%d/.command.out", task.getTaskId()), taskOut);
                } catch (ApiException e) {
                    // Ignore error 404 that means that the file is no longer available
                    // Ignore error 400 that means that the run was launch using Nextflow CLI
                    if (e.getCode() != 404 && e.getCode() != 400) {
                        throw e;
                    }
                }

                try {
                    File taskOut = api().downloadWorkflowTaskLog(workflowId, task.getTaskId(), ".command.err", wspId);
                    addFile(out, String.format("tasks/%d/.command.err", task.getTaskId()), taskOut);
                } catch (ApiException e) {
                    // Ignore error 404 that means that the file is no longer available
                    // Ignore error 400 that means that the run was launch using Nextflow CLI
                    if (e.getCode() != 404 && e.getCode() != 400) {
                        throw e;
                    }
                }

                try {
                    File taskOut = api().downloadWorkflowTaskLog(workflowId, task.getTaskId(), ".command.log", wspId);
                    addFile(out, String.format("tasks/%d/.command.log", task.getTaskId()), taskOut);
                } catch (ApiException e) {
                    // Ignore error 404 that means that the file is no longer available
                    // Ignore error 400 that means that the run was launch using Nextflow CLI
                    if (e.getCode() != 404 && e.getCode() != 400) {
                        throw e;
                    }
                }
            }

            if (addFusionLogs) {
                try {
                    File taskOut = api().downloadWorkflowTaskLog(workflowId, task.getTaskId(), ".fusion.log", wspId);
                    addFile(out, String.format("tasks/%d/.fusion.log", task.getTaskId()), taskOut);
                } catch (ApiException e) {
                    // Ignore error 404 that means that the file is no longer available
                    // Ignore error 400 that means that the run was launch using Nextflow CLI
                    if (e.getCode() != 404 && e.getCode() != 400) {
                        throw e;
                    }
                }
            }
        }
    }

    private List<Task> loadTasks(Long wspId, String workflowId) throws ApiException {
        int max = 100;
        int offset = 0;
        int added = max;

        List<Task> tasks = new ArrayList<>();
        while (added == max) {
            added = 0;
            ListTasksResponse response = api().listWorkflowTasks(workflowId, wspId, max, offset, null, null, null);
            for (DescribeTaskResponse describeTaskResponse : Objects.requireNonNull(response.getTasks())) {
                Task task = describeTaskResponse.getTask();
                tasks.add(task);
                added++;
            }
            offset += max;
        }
        return tasks;
    }

    private <T> void addEntry(TarArchiveOutputStream out, String fileName, Class<T> type, T value) throws IOException {
        if (value == null) {
            return;
        }
        addEntry(out, fileName, toJSON(type, value));
    }

    private void addEntry(TarArchiveOutputStream out, String fileName, byte[] data) throws IOException {
        if (data == null) {
            return;
        }
        TarArchiveEntry entry = new TarArchiveEntry(fileName);
        entry.setSize(data.length);
        out.putArchiveEntry(entry);
        out.write(data);
        out.closeArchiveEntry();
    }

    private void addFile(TarArchiveOutputStream out, String fileName, File file) throws IOException {
        if (file == null) {
            return;
        }
        compressExecutor.submit(new AddEntry(out, fileName, file));
    }

    private <T> byte[] toJSON(Class<T> type, T value) throws JsonProcessingException {
        return JSON
                .getContext(type)
                .writerWithDefaultPrettyPrinter()
                .writeValueAsBytes(value);
    }

    private String generateUrl(Long wspId, String userName, String wfId) throws ApiException {
        if (wspId == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName, wfId);
        }
        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(wspId), workspaceName(wspId), wfId);
    }

}

