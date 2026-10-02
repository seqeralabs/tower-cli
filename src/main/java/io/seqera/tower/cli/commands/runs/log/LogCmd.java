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


package io.seqera.tower.cli.commands.runs.log;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.runs.AbstractRunsCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunLog;
import io.seqera.tower.model.LogPage;
import picocli.CommandLine;

@CommandLine.Command(
        name = "log",
        description = "Display the execution log of a pipeline run or task"
)
public class LogCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-t"}, description = "Task numeric identifier. When specified, displays the task output log instead of the Nextflow head job output.")
    public Long task;

    @CommandLine.ParentCommand
    ViewCmd parentCommand;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(parentCommand.workspace.workspace);

        // Platform serves the compute platform's live log stream when it has one, so unlike 'download' this
        // also works while the run is active; otherwise it falls back to the output file.
        LogPage page = task == null
                ? workflowsApi().getWorkflowLog(parentCommand.id, wspId, null, null).getLog()
                : workflowsApi().getWorkflowTaskLog(parentCommand.id, task, null, wspId, null).getLog();

        return new RunLog(page.getEntries(), Boolean.TRUE.equals(page.getTruncated()), page.getMessage());
    }
}
