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
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesUpdated;
import io.seqera.tower.cli.utils.FilesHelper;
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
import java.util.Collections;

import static io.seqera.tower.cli.utils.ModelHelper.coalesce;
import static io.seqera.tower.cli.utils.ModelHelper.removeEmptyValues;

@Command(
        name = "update",
        description = "Update a workspace pipeline."
)
public class UpdateCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"-d", "--description"}, description = "Pipeline description.")
    public String description;

    @Option(names = {"--new-name"}, description = "Pipeline new name.")
    public String newName;

    @Mixin
    public LaunchOptions opts;

    @Option(names = {"--pipeline"}, description = "Nextflow pipeline URL.")
    public String pipeline;

    @Override
    protected Response exec() throws ApiException, IOException {

        Long wspId = workspaceId(workspace.workspace);
        Long orgId = wspId != null ? orgId(wspId) : null;
        PipelineDbDto pipe;
        Long id;

        if (pipelineRefOptions.pipeline.pipelineId != null) {
            id = pipelineRefOptions.pipeline.pipelineId;
            pipe = api().describePipeline(id, Collections.emptyList(), wspId, null).getPipeline();
        } else {
            pipe = pipelineByName(wspId, pipelineRefOptions.pipeline.pipelineName);
            id = pipe.getPipelineId();
        }

        if (newName != null) {
            try {
                api().validatePipelineName(wspId, orgId, newName);
            } catch (ApiException ex) {
                throw new InvalidResponseException(String.format("Pipeline name '%s' is not valid", newName));
            }
        }

        Long sourceWorkspaceId = sourceWorkspaceId(wspId, pipe);
        Launch launch = api().describePipelineLaunch(id, wspId, sourceWorkspaceId).getLaunch();

        // Retrieve the provided computeEnv or use the primary if not provided
        String ceId = opts.computeEnv != null ? computeEnvByRef(wspId, opts.computeEnv).getId() : launch.getComputeEnv().getId();

        UpdatePipelineRequest updateReq = new UpdatePipelineRequest()
                .name(coalesce(newName, pipe.getName()))
                .description(coalesce(description, pipe.getDescription()))
                .launch(new WorkflowLaunchRequest()
                        .computeEnvId(ceId)
                        .pipeline(coalesce(pipeline, launch.getPipeline()))
                        .revision(coalesce(opts.revision, launch.getRevision()))
                        .workDir(coalesce(opts.workDir, launch.getWorkDir()))
                        .configProfiles(coalesce(opts.profile, launch.getConfigProfiles()))
                        .paramsText(coalesce(FilesHelper.readString(opts.paramsFile), launch.getParamsText()))

                        // Advanced options
                        .configText(coalesce(FilesHelper.readString(opts.config), launch.getConfigText()))
                        .preRunScript(coalesce(FilesHelper.readString(opts.preRunScript), launch.getPreRunScript()))
                        .postRunScript(coalesce(FilesHelper.readString(opts.postRunScript), launch.getPostRunScript()))
                        .pullLatest(coalesce(opts.pullLatest, launch.getPullLatest()))
                        .stubRun(coalesce(opts.stubRun, launch.getStubRun()))
                        .mainScript(coalesce(opts.mainScript, launch.getMainScript()))
                        .entryName(coalesce(opts.entryName, launch.getEntryName()))
                        .schemaName(coalesce(opts.schemaName, launch.getSchemaName()))
                        .userSecrets(coalesce(removeEmptyValues(opts.userSecrets), launch.getUserSecrets()))
                        .workspaceSecrets(coalesce(removeEmptyValues(opts.workspaceSecrets), launch.getWorkspaceSecrets()))
                );

        api().updatePipeline(pipe.getPipelineId(), updateReq, wspId);

        return new PipelinesUpdated(workspaceRef(wspId), pipe.getName());
    }
}
