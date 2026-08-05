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
import io.seqera.tower.model.WorkflowLaunchRequest;
import io.seqera.tower.model.WorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowMaxDbDto;
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
        description = "Relaunch a pipeline run"
)
public class RelaunchCmd extends AbstractRunsCmd {

    @Option(names = {"-i", "--id"}, description = "Pipeline run identifier to relaunch", required = true)
    public String id;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"--pipeline"}, description = "Override the pipeline to launch. Allows relaunching with a different pipeline repository URL while keeping other launch configuration settings.")
    public String pipeline;

    @Option(names = {"--no-resume"}, description = "Start workflow execution from scratch instead of resuming from the last successful process. Use this to rerun the entire workflow without using cached results.")
    public boolean noResume;

    @Option(names = {"-n", "--name"}, description = "Custom workflow run name. Overrides the automatically generated run name with a user-defined identifier.")
    public String name;

    @Option(names = {"--launch-container"}, description = "Container image for the Nextflow head job. Overrides the default launcher container.")
    public String launchContainer;

    @Mixin
    public LaunchOptions opts;

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        if (!noResume && opts.workDir != null) {
            throw new TowerException("Not allowed to change '--work-dir' option when resuming. Use '--no-resume' if you want to relaunch into a different working directory without resuming.");
        }

        if (!noResume && pipeline != null) {
            throw new TowerException("Not allowed to change '--pipeline' option when resuming. Use '--no-resume' if you want to relaunch a different pipeline without resuming.");
        }

        WorkflowMaxDbDto workflow = workflowById(wspId, id, NO_WORKFLOW_ATTRIBUTES).getWorkflow();
        WorkflowLaunchResponse launch = workflowLaunchResponse(workflow.getId(), wspId);

        ComputeEnvResponseDto ce = null;
        if (opts.computeEnv != null) {
            ce = computeEnvByRef(wspId, opts.computeEnv);
        }

        // A run can only be resumed from the commit ID it reported: without it there is no
        // revision to resume from, and silently relaunching from scratch would charge the user
        // for a full rerun while reporting a resume.
        if (!noResume && launch.getResumeCommitId() == null) {
            throw new TowerException(String.format("Pipeline run '%s' cannot be resumed because it did not report a commit ID. Use '--no-resume' to relaunch it from scratch.", id));
        }

        WorkflowLaunchRequest workflowLaunchRequest = new WorkflowLaunchRequest()
                .id(workflow.getLaunchId())
                .sessionId(launch.getSessionId())
                .computeEnvId(ce != null ? ce.getId() : launch.getComputeEnv().getId())
                .pipeline(coalesce(pipeline, launch.getPipeline()))
                .workDir(opts.workDir != null ? opts.workDir : selectWorkDir(!noResume, launch.getResumeDir(), launch.getWorkDir(), workflow.getWorkDir()))
                .revision(coalesce(opts.revision, (noResume ? launch.getRevision() : launch.getResumeCommitId())))
                // Platform never inherits the commit ID from the source launch: an unset value means
                // "unpinned", so only an explicit --commit-id pins the relaunch to a given commit.
                .commitId(opts.commitId)
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
                // Platform reads these from the incoming request only, without falling back to the
                // source launch, so they have to be carried over explicitly or they are lost.
                .headJobCpus(launch.getHeadJobCpus())
                .headJobMemoryMb(launch.getHeadJobMemoryMb())
                .optimizationId(launch.getOptimizationId())
                .optimizationTargets(launch.getOptimizationTargets())
                .dateCreated(OffsetDateTime.now())
                .runName(name)
                .launchContainer(launchContainer)
                ;

        if (!noResume) {
            workflowLaunchRequest.sessionId(workflow.getSessionId());
        }

        SubmitWorkflowLaunchRequest submitWorkflowLaunchRequest = new SubmitWorkflowLaunchRequest()
                .launch(workflowLaunchRequest);

        SubmitWorkflowLaunchResponse response = workflowsApi().createWorkflowLaunch(submitWorkflowLaunchRequest, wspId, null);

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
        DescribeWorkflowLaunchResponse launchResponse = workflowsApi().describeWorkflowLaunch(workflowId, workspaceId);
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
