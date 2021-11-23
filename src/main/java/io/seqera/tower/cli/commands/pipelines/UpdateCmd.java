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
import io.seqera.tower.cli.responses.pipelines.PipelinesUpdated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.UpdatePipelineRequest;
import io.seqera.tower.model.UpdatePipelineResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;

import static io.seqera.tower.cli.utils.ModelHelper.coalesce;

@Command(
        name = "update",
        description = "Update a workspace pipeline."
)
public class UpdateCmd extends AbstractPipelinesCmd {

    @Option(names = {"-n", "--name"}, description = "Pipeline name.", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"-d", "--description"}, description = "Pipeline description.")
    public String description;

    @Mixin
    public LaunchOptions opts;

    @Option(names = {"--pipeline"}, description = "Nextflow pipeline URL.")
    public String pipeline;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        PipelineDbDto pipe = pipelineByName(wspId, name);

        // Retrieve current launch
        Launch launch = api().describePipelineLaunch(pipe.getPipelineId(), wspId).getLaunch();

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByName(wspId, opts.computeEnv) : launch.getComputeEnv();

        UpdatePipelineResponse response = api().updatePipeline(
                pipe.getPipelineId(),
                new UpdatePipelineRequest()
                        .description(coalesce(description, pipe.getDescription()))
                        .launch(new WorkflowLaunchRequest()
                                .computeEnvId(ce.getId())
                                .pipeline(coalesce(pipeline, launch.getPipeline()))
                                .revision(coalesce(opts.revision, launch.getRevision()))
                                .workDir(coalesce(opts.workDir, launch.getWorkDir()))
                                .configProfiles(coalesce(opts.profiles, launch.getConfigProfiles()))
                                .paramsText(coalesce(FilesHelper.readString(opts.params), launch.getParamsText()))

                                // Advanced options
                                .configText(coalesce(FilesHelper.readString(opts.config), launch.getConfigText()))
                                .preRunScript(coalesce(FilesHelper.readString(opts.preRunScript), launch.getPreRunScript()))
                                .postRunScript(coalesce(FilesHelper.readString(opts.postRunScript), launch.getPostRunScript()))
                                .pullLatest(coalesce(opts.pullLatest, launch.getPullLatest()))
                                .stubRun(coalesce(opts.stubRun, launch.getStubRun()))
                                .mainScript(coalesce(opts.mainScript, launch.getMainScript()))
                                .entryName(coalesce(opts.entryName, launch.getEntryName()))
                                .schemaName(coalesce(opts.schemaName, launch.getSchemaName()))
                        )
                , wspId
        );

        return new PipelinesUpdated(workspaceRef(wspId), response.getPipeline().getName());
    }
}
