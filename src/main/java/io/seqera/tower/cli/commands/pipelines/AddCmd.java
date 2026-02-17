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
import io.seqera.tower.cli.commands.labels.LabelsOptionalOptions;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesAdded;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.CreatePipelineRequest;
import io.seqera.tower.model.CreatePipelineResponse;
import io.seqera.tower.model.Visibility;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;

import static io.seqera.tower.cli.utils.ModelHelper.removeEmptyValues;

@Command(
        name = "add",
        description = "Add a pipeline"
)
public class AddCmd extends AbstractPipelinesCmd {

    @Option(names = {"-n", "--name"}, description = "Pipeline name. Must be unique within the workspace.", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"-d", "--description"}, description = "Pipeline description.")
    public String description;

    @Parameters(index = "0", paramLabel = "PIPELINE_URL", description = "Nextflow pipeline URL", arity = "1")
    public String pipeline;

    @Mixin
    public LabelsOptionalOptions labels;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        // Check workspace visibility
        Visibility visibility = Visibility.PRIVATE;
        if (wspId != null) {
            visibility = workspacesApi().describeWorkspace(orgId(wspId), wspId).getWorkspace().getVisibility();
        }

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnvResponseDto ce = opts.computeEnv != null ? computeEnvByRef(wspId, opts.computeEnv) : null;

        // By default use primary compute environment at private workspaces
        if (ce == null && visibility == Visibility.PRIVATE) {
            ce = primaryComputeEnv(wspId);
            if (ce == null) {
                throw new ApiException("No compute environment. You need to provide one using '--compute-env' or define a workspace primary one.");
            }
        }

        // Use compute env values by default
        String workDirValue = opts.workDir == null && ce != null? ce.getConfig().getWorkDir() : opts.workDir;
        String preRunScriptValue = opts.preRunScript == null && ce != null ? ce.getConfig().getPreRunScript() : FilesHelper.readString(opts.preRunScript);
        String postRunScriptValue = opts.postRunScript == null && ce != null ? ce.getConfig().getPostRunScript() : FilesHelper.readString(opts.postRunScript);

        CreatePipelineResponse response = pipelinesApi().createPipeline(
                new CreatePipelineRequest()
                        .name(name)
                        .description(description)
                        .launch(new WorkflowLaunchRequest()
                                .computeEnvId(ce != null ? ce.getId() : null)
                                .pipeline(pipeline)
                                .revision(opts.revision)
                                .commitId(opts.commitId)
                                .workDir(workDirValue)
                                .configProfiles(opts.profile)
                                .paramsText(FilesHelper.readString(opts.paramsFile))

                                // Advanced options
                                .configText(FilesHelper.readString(opts.config))
                                .preRunScript(preRunScriptValue)
                                .postRunScript(postRunScriptValue)
                                .pullLatest(opts.pullLatest)
                                .stubRun(opts.stubRun)
                                .mainScript(opts.mainScript)
                                .entryName(opts.entryName)
                                .schemaName(opts.schemaName)
                                .userSecrets(removeEmptyValues(opts.userSecrets))
                                .workspaceSecrets(removeEmptyValues(opts.workspaceSecrets))
                        )
                , wspId
        );

        attachLabels(wspId,response.getPipeline().getPipelineId());

        return new PipelinesAdded(workspaceRef(wspId), response.getPipeline().getName());
    }

    private void attachLabels(Long wspId,Long pipelineId) throws ApiException{
        PipelinesLabelsManager creator = new PipelinesLabelsManager(labelsApi());
        creator.execute(wspId, pipelineId, labels.labels);
    }
}
