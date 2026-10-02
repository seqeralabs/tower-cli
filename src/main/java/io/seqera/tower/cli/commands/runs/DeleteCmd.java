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


package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunDeleted;
import io.seqera.tower.cli.responses.runs.RunsDeleted;
import io.seqera.tower.model.DeleteWorkflowsRequest;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@CommandLine.Command(
        name = "delete",
        description = "Delete one or more pipeline runs"
)
public class DeleteCmd extends AbstractRunsCmd {

    @CommandLine.Option(names = {"-i", "-id"}, split = ",", description = "Pipeline run identifier. The unique workflow ID to delete. Deletes the run record and associated metadata from Seqera Platform. Repeat the option or provide a comma-separated list to delete several runs at once.", required = true)
    public List<String> ids;

    @CommandLine.Option(names = {"--force"}, description = "Force deletion of active workflows. By default, only completed workflows can be deleted. Use this flag to delete running or pending workflows.")
    public boolean force = false;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        if (ids.size() == 1) {
            workflowsApi().deleteWorkflow(ids.getFirst(), wspId, force);
            return new RunDeleted(ids.getFirst(), workspaceRef(wspId));
        }

        // The bulk endpoint reports the runs it could not delete instead of failing the whole request.
        List<String> failed = Objects.requireNonNullElse(workflowsApi()
                .deleteWorkflowMany(new DeleteWorkflowsRequest().workflowIds(ids), wspId, force)
                .getFailedWorkflowIds(), List.of());
        List<String> deleted = ids.stream().filter(id -> !failed.contains(id)).toList();
        return new RunsDeleted(deleted, failed, workspaceRef(wspId));
    }
}
