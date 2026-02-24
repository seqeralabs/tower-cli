/*
 * Copyright 2021-2026, Seqera.
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
 */

package io.seqera.tower.cli.commands.runs.download;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.commands.runs.download.enums.RunDownloadFileType;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunFileDownloaded;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

@CommandLine.Command(
        name = "download",
        description = "Download pipeline run files"
)
public class DownloadCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"--type"}, description = "Type of file to download. Options: 'stdout' (standard output), 'log' (Nextflow log), 'stderr' (standard error, tasks only), 'timeline' (execution timeline HTML, workflow only). Default: stdout.", defaultValue = "stdout")
    public RunDownloadFileType type;

    @CommandLine.Option(names = {"-t"}, description = "Task numeric identifier. When specified, downloads task-specific files (.command.out, .command.err, .command.log). When omitted, downloads workflow-level files (nextflow.log, timeline.html).")
    public Long task;

    @CommandLine.ParentCommand
    ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(parentCommand.workspace.workspace);

        String fileName;
        File file;

        if (task == null) {
            fileName = String.format("nf-%s.txt", parentCommand.id);

            if (type == RunDownloadFileType.log) {
                fileName = String.format("nf-%s.log", parentCommand.id);
            } else if (type == RunDownloadFileType.timeline) {
                fileName = String.format("timeline-%s.html", parentCommand.id);
            } else if (type == RunDownloadFileType.stderr) {
                throw new TowerException("Error file is not available for pipeline's runs");
            }

            file = workflowsApi().downloadWorkflowLog(parentCommand.id, fileName, wspId);

        } else {
            fileName = ".command.out";

            if (type == RunDownloadFileType.log) {
                fileName = ".command.log";
            } else if (type == RunDownloadFileType.stderr) {
                fileName = ".command.err";
            } else if (type == RunDownloadFileType.timeline) {
                throw new TowerException("Timeline file is not available for tasks");
            }

            file = workflowsApi().downloadWorkflowTaskLog(parentCommand.id, task, fileName, wspId);
        }

        return new RunFileDownloaded(file, type);
    }
}
