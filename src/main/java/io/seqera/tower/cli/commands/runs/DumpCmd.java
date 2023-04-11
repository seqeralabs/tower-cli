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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.ApiException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunDump;
import io.seqera.tower.cli.utils.SilentPrintWriter;
import io.seqera.tower.model.DescribeTaskResponse;
import io.seqera.tower.model.DescribeWorkflowResponse;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListTasksResponse;
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

        DescribeWorkflowResponse workflowResponse = workflowById(wspId, id);
        Workflow workflow = workflowResponse.getWorkflow();
        if (workflow == null) {
            throw new TowerException("Unknown workflow");
        }
        WorkflowLoad workflowLoad = workflowLoadByWorkflowId(wspId, id);
        Launch launch = workflow.getLaunchId() != null ? launchById(wspId, workflow.getLaunchId()) : null;
        List<WorkflowMetrics> metrics = api().describeWorkflowMetrics(workflow.getId(), wspId).getMetrics();
        addEntry(out, "workflow.json", Workflow.class, workflow);
        addEntry(out, "workflow-load.json", WorkflowLoad.class, workflowLoad);
        addEntry(out, "workflow-launch.json", Launch.class, launch);
        addEntry(out, "workflow-metrics.json", List.class, metrics);

        // Files
        try {
            File nextflowLog = api().downloadWorkflowLog(workflow.getId(), String.format("nf-%s.log", workflow.getId()), wspId);
            addFile(out, "nextflow.log", nextflowLog);
        } catch (ApiException e) {
            // Ignore error 404 that means that the file it is no longer available
            if (e.getCode() != 404) {
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
                    // Ignore error 404 that means that the file it is no longer available
                    if (e.getCode() != 404) {
                        throw e;
                    }
                }

                try {
                    File taskOut = api().downloadWorkflowTaskLog(workflowId, task.getTaskId(), ".command.err", wspId);
                    addFile(out, String.format("tasks/%d/.command.err", task.getTaskId()), taskOut);
                } catch (ApiException e) {
                    // Ignore error 404 that means that the file it is no longer available
                    if (e.getCode() != 404) {
                        throw e;
                    }
                }

                try {
                    File taskOut = api().downloadWorkflowTaskLog(workflowId, task.getTaskId(), ".command.log", wspId);
                    addFile(out, String.format("tasks/%d/.command.log", task.getTaskId()), taskOut);
                } catch (ApiException e) {
                    // Ignore error 404 that means that the file it is no longer available
                    if (e.getCode() != 404) {
                        throw e;
                    }
                }
            }

            if (addFusionLogs) {
                try {
                    File taskOut = api().downloadWorkflowTaskLog(workflowId, task.getTaskId(), ".fusion.log", wspId);
                    addFile(out, String.format("tasks/%d/.fusion.log", task.getTaskId()), taskOut);
                } catch (ApiException e) {
                    // Ignore error 404 that means that the file it is no longer available
                    if (e.getCode() != 404) {
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
        TarArchiveEntry entry = new TarArchiveEntry(fileName);
        byte[] data = toJSON(type, value);
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
}

