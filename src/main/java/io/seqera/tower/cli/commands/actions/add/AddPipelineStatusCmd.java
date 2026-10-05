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

package io.seqera.tower.cli.commands.actions.add;

import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.model.ActionSource;
import io.seqera.tower.model.CreateActionRequest;
import io.seqera.tower.model.PipelineStatusActionRequest;
import io.seqera.tower.model.WorkflowStatus;
import picocli.CommandLine;

@CommandLine.Command(
        name = "pipeline-status",
        description = "Add a pipeline action triggered when a pipeline run reaches a given state"
)
public class AddPipelineStatusCmd extends AbstractAddCmd {

    @CommandLine.Option(names = {"--watch-pipeline-id"}, description = "Launchpad pipeline identifier whose runs trigger the action.", required = true)
    public Long watchPipelineId;

    @CommandLine.Option(names = {"--run-status"}, description = "Run state of the watched pipeline that triggers the action: SUCCEEDED, FAILED or CANCELLED.", required = true)
    public WorkflowStatus runStatus;

    @Override
    protected ActionSource getSource() {
        return ActionSource.pipeline_status;
    }

    @Override
    protected void configureTrigger(CreateActionRequest request) throws TowerException {
        checkTriggerRunStatus(runStatus);
        request.setPipelineStatus(new PipelineStatusActionRequest().pipelineId(watchPipelineId).runStatus(runStatus));
    }
}
