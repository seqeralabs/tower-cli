/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesUpdated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.UpdatePipelineRequest;
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
        description = "Update a pipeline"
)
public class UpdateCmd extends AbstractPipelinesCmd {

    @CommandLine.Mixin
    PipelineRefOptions pipelineRefOptions;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"-d", "--description"}, description = "Pipeline description")
    public String description;

    @Option(names = {"--new-name"}, description = "Pipeline new name")
    public String newName;

    @Mixin
    public LaunchOptions opts;

    @Option(names = {"--pipeline-schema-id"}, description = "Pipeline schema identifier to use.")
    public Long pipelineSchemaId;

    @Option(names = {"--pipeline"}, description = "Nextflow pipeline URL")
    public String pipeline;

    @Override
    protected Response exec() throws ApiException, IOException {

        Long wspId = workspaceId(workspace.workspace);
        Long orgId = wspId != null ? orgId(wspId) : null;
        PipelineDbDto pipe;
        Long id;

        if (pipelineRefOptions.pipeline.pipelineId != null) {
            id = pipelineRefOptions.pipeline.pipelineId;
            pipe = pipelinesApi().describePipeline(id, Collections.emptyList(), wspId, null).getPipeline();
        } else {
            pipe = pipelineByName(wspId, pipelineRefOptions.pipeline.pipelineName);
            id = pipe.getPipelineId();
        }

        if (newName != null) {
            try {
                pipelinesApi().validatePipelineName(wspId, orgId, newName);
            } catch (ApiException ex) {
                throw new InvalidResponseException(String.format("Pipeline name '%s' is not valid", newName));
            }
        }

        Long sourceWorkspaceId = sourceWorkspaceId(wspId, pipe);
        LaunchDbDto launch = pipelinesApi().describePipelineLaunch(id, wspId, sourceWorkspaceId, null).getLaunch();
        // Retrieve the provided computeEnv or use the primary if not provided
        String ceId = null;
        if (opts.computeEnv != null) {
            ceId = computeEnvByRef(wspId, opts.computeEnv).getId();
        } else {
            final var ce = launch.getComputeEnv();
            if (ce != null) {
                ceId = ce.getId();
            }
        }

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
                        .pipelineSchemaId(coalesce(pipelineSchemaId, launch.getPipelineSchemaId()))
                        .userSecrets(coalesce(removeEmptyValues(opts.userSecrets), launch.getUserSecrets()))
                        .workspaceSecrets(coalesce(removeEmptyValues(opts.workspaceSecrets), launch.getWorkspaceSecrets()))
                );

        pipelinesApi().updatePipeline(pipe.getPipelineId(), updateReq, wspId);

        return new PipelinesUpdated(workspaceRef(wspId), pipe.getName());
    }
}
