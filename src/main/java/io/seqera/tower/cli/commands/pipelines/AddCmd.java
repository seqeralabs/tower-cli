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
import io.seqera.tower.cli.responses.pipelines.PipelinesAdded;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
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

@Command(
        name = "add",
        description = "Add a workspace pipeline."
)
public class AddCmd extends AbstractPipelinesCmd {

    @Option(names = {"-n", "--name"}, description = "Pipeline name.", required = true)
    public String name;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"-d", "--description"}, description = "Pipeline description.")
    public String description;

    @Parameters(index = "0", paramLabel = "PIPELINE_URL", description = "Nextflow pipeline URL.", arity = "1")
    public String pipeline;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);
        Long orgId = orgId(wspId);

        // Check workspace visibility
        Visibility visibility = api().describeWorkspace(orgId, wspId).getWorkspace().getVisibility();

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = opts.computeEnv != null ? computeEnvByRef(wspId, opts.computeEnv) : null;

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

        CreatePipelineResponse response = api().createPipeline(
                new CreatePipelineRequest()
                        .name(name)
                        .description(description)
                        .launch(new WorkflowLaunchRequest()
                                .computeEnvId(ce != null ? ce.getId() : null)
                                .pipeline(pipeline)
                                .revision(opts.revision)
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
                        )
                , wspId
        );

        return new PipelinesAdded(workspaceRef(wspId), response.getPipeline().getName());
    }
}
