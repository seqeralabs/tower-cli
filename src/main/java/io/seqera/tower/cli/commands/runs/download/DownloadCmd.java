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
        description = "Download a pipeline's run related files."
)
public class DownloadCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"--type"}, description = "File type to download. Options are stdout, log, stderr (for tasks only) and timeline (workflow only) (default is stdout).", defaultValue = "stdout")
    public RunDownloadFileType type;

    @CommandLine.Option(names = {"-t"}, description = "Task identifier.")
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

            file = api().downloadWorkflowLog(parentCommand.id, fileName, wspId);

        } else {
            fileName = ".command.out";

            if (type == RunDownloadFileType.log) {
                fileName = ".command.log";
            } else if (type == RunDownloadFileType.stderr) {
                fileName = ".command.err";
            } else if (type == RunDownloadFileType.timeline) {
                throw new TowerException("Timeline file is not available for tasks");
            }

            file = api().downloadWorkflowTaskLog(parentCommand.id, task, fileName, wspId);
        }

        return new RunFileDownloaded(file, type);
    }
}
