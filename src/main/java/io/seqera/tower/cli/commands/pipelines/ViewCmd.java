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

package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesView;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.PipelineDbDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "view",
        description = "View pipeline details."
)
public class ViewCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Override
    protected Response exec() throws ApiException {
        Long wspId = workspaceId(workspace.workspace);
        PipelineDbDto pipeline = fetchPipeline(pipelineRefOptions, wspId);
        Long sourceWorkspaceId = sourceWorkspaceId(wspId, pipeline);

        Launch launch = api().describePipelineLaunch(pipeline.getPipelineId(), wspId, sourceWorkspaceId).getLaunch();

        return new PipelinesView(workspaceRef(wspId), pipeline, launch, baseWorkspaceUrl(wspId));
    }
}
