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
import io.seqera.tower.cli.exceptions.LaunchNotFoundException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunSubmited;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.DescribeWorkflowLaunchResponse;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.Workflow;
import io.seqera.tower.model.WorkflowLaunchRequest;
import io.seqera.tower.model.WorkflowLaunchResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.time.OffsetDateTime;

import static io.seqera.tower.cli.utils.ModelHelper.coalesce;
import static io.seqera.tower.cli.utils.ModelHelper.removeEmptyValues;

@Command(
        name = "relaunch",
        description = "Add a pipeline run."
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

    @Option(names = {"-n", "--name"}, description = "Custom workflow run name")
    public String name;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        if (!noResume && opts.workDir != null) {
            throw new TowerException("Not allowed to change '--work-dir' option when resuming. Use '--no-resume' if you want to relaunch into a different working directory without resuming.");
        }

        Workflow workflow = workflowById(wspId, id).getWorkflow();
        WorkflowLaunchResponse launch = workflowLaunchResponse(workflow.getId(), wspId);

        ComputeEnvResponseDto ce = null;
        if (opts.computeEnv != null) {
            ce = computeEnvByRef(wspId, opts.computeEnv);
        }

        // Check if it's not possible to resume the workflow
        if (launch.getResumeCommitId() == null) {
            noResume = true;
        }

        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest()
                .id(workflow.getLaunchId())
                .sessionId(launch.getSessionId())
                .computeEnvId(ce != null ? ce.getId() : launch.getComputeEnv().getId())
                .pipeline(coalesce(pipeline, launch.getPipeline()))
                .workDir(opts.workDir != null ? opts.workDir : selectWorkDir(!noResume, launch.getResumeDir(), launch.getWorkDir(), workflow.getWorkDir()))
                .revision(coalesce(opts.revision, (noResume ? launch.getRevision() : launch.getResumeCommitId())))
                .configProfiles(coalesce(opts.profile, launch.getConfigProfiles()))
                .configText(opts.config != null ? FilesHelper.readString(opts.config) : launch.getConfigText())
                .paramsText(opts.paramsFile != null ? FilesHelper.readString(opts.paramsFile) : launch.getParamsText())
                .preRunScript(opts.preRunScript != null ? FilesHelper.readString(opts.preRunScript) : launch.getPreRunScript())
                .postRunScript(opts.postRunScript != null ? FilesHelper.readString(opts.postRunScript) : launch.getPostRunScript())
                .mainScript(coalesce(opts.mainScript, launch.getMainScript()))
                .entryName(coalesce(opts.entryName,  launch.getEntryName()))
                .schemaName(coalesce(opts.schemaName, launch.getSchemaName()))
                .userSecrets(coalesce(removeEmptyValues(opts.userSecrets), launch.getUserSecrets()))
                .workspaceSecrets(coalesce(removeEmptyValues(opts.workspaceSecrets), launch.getWorkspaceSecrets()))
                .resume(!noResume)
                .pullLatest(coalesce(opts.pullLatest, launch.getPullLatest()))
                .stubRun(coalesce(opts.stubRun, launch.getStubRun()))
                .dateCreated(OffsetDateTime.now())
                .runName(name);

        if (!noResume) {
            workflowLaunchRequest.sessionId(workflow.getSessionId());
        }

        SubmitWorkflowLaunchRequest submitWorkflowLaunchRequest = new SubmitWorkflowLaunchRequest()
                .launch(workflowLaunchRequest);

        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(submitWorkflowLaunchRequest, wspId, null,null);

        return new RunSubmited(response.getWorkflowId(), wspId, workflowWatchUrl(response.getWorkflowId(), wspId), workspaceRef(wspId));
    }

    private String selectWorkDir(boolean isResume, String launchResumeDir, String launchWorkDir, String workflowWorkDir) {
        if (isResume) {
            return launchResumeDir;
        }
        if (launchWorkDir != null) {
            return launchWorkDir;
        }
        return workflowWorkDir;
    }

    private WorkflowLaunchResponse workflowLaunchResponse(String workflowId, Long workspaceId) throws ApiException {
        DescribeWorkflowLaunchResponse launchResponse = api().describeWorkflowLaunch(workflowId, workspaceId);
        if (launchResponse == null) {
            throw new LaunchNotFoundException(id, workspaceRef(workspaceId));
        }
        return launchResponse.getLaunch();
    }

    private String workflowWatchUrl(String workflowId, Long wspId) throws ApiException {

        if (wspId == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName(), workflowId);
        }

        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(wspId), workspaceName(wspId), workflowId);
    }
}
