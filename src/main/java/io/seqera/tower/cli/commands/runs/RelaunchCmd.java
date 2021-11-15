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

package io.seqera.tower.cli.commands.runs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.LaunchOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunSubmited;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.time.OffsetDateTime;

@Command(
        name = "relaunch",
        description = "Create a pipeline run."
)
public class RelaunchCmd extends AbstractRunsCmd {

    @Option(names = {"-i", "--id"}, description = "Pipeline run id to relaunch.", required = true)
    public String id;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"--pipeline"}, description = "Pipeline to launch.")
    public String pipeline;

    @Option(names = {"--no-resume"}, description = "Do not resume the pipeline run.")
    public boolean noResume;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {

        if (!noResume && opts.workDir != null) {
            throw new TowerException("Not allowed to change '--work-dir' option when resuming. Use '--no-resume' if you want to relaunch into a different working directory without resuming.");
        }

        Workflow workflow = workflowById(workspace.workspaceId, id);
        Launch launch = launchById(workspace.workspaceId, workflow.getLaunchId());

        ComputeEnv ce = null;
        if (opts.computeEnv != null) {
            ce = computeEnvByName(workspace.workspaceId, opts.computeEnv);
        }

        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest()
                .id(workflow.getLaunchId())
                .sessionId(launch.getSessionId())
                .computeEnvId(ce != null ? ce.getId() : launch.getComputeEnv().getId())
                .pipeline(pipeline != null ? pipeline : launch.getPipeline())
                .workDir(opts.workDir != null ? opts.workDir : workflow.getWorkDir())
                .revision(opts.revision != null ? opts.revision : workflow.getRevision())
                .configProfiles(opts.profiles != null ? opts.profiles : launch.getConfigProfiles())
                .configText(opts.config != null ? FilesHelper.readString(opts.config) : workflow.getConfigText())
                .paramsText(opts.params != null ? FilesHelper.readString(opts.params) : launch.getParamsText())
                .preRunScript(opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : launch.getPreRunScript())
                .postRunScript(opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : launch.getPostRunScript())
                .mainScript(opts.mainScript != null ? opts.mainScript : launch.getPostRunScript())
                .entryName(opts.entryName != null ? opts.entryName : launch.getEntryName())
                .schemaName(opts.schemaName != null ? opts.schemaName : launch.getSchemaName())
                .resume(!noResume)
                .pullLatest(opts.pullLatest != null ? opts.pullLatest : launch.getPullLatest())
                .stubRun(opts.stubRun != null ? opts.stubRun : launch.getStubRun())
                .dateCreated(OffsetDateTime.now());

        if (!noResume) {
            workflowLaunchRequest.sessionId(workflow.getSessionId());
        }

        SubmitWorkflowLaunchRequest submitWorkflowLaunchRequest = new SubmitWorkflowLaunchRequest()
                .launch(workflowLaunchRequest);

        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(submitWorkflowLaunchRequest, workspace.workspaceId);

        return new RunSubmited(response.getWorkflowId(), workflowWatchUrl(response.getWorkflowId()), workspaceRef(workspace.workspaceId));
    }

    private String workflowWatchUrl(String workflowId) throws ApiException {

        if (workspace.workspaceId == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName(), workflowId);
        }

        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(workspace.workspaceId), workspaceName(workspace.workspaceId), workflowId);
    }
}
