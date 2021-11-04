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
import io.seqera.tower.cli.commands.global.WorkspaceOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesDeleted;
import io.seqera.tower.model.PipelineDbDto;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(
        name = "delete",
        description = "Delete a workspace pipeline"
)
public class DeleteCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    public WorkspaceOptions workspace;

    @Option(names = {"-n", "--name"}, description = "Pipeline name", required = true)
    public String name;

    @Override
    protected Response exec() throws ApiException, IOException {
        PipelineDbDto pipe = pipelineByName(workspace.workspaceId, name);
        api().deletePipeline(pipe.getPipelineId(), workspace.workspaceId);
        return new PipelinesDeleted(pipe.getName(), workspaceRef(workspace.workspaceId));
    }
}
