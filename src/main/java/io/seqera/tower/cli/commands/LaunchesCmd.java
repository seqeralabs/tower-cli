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

package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptions;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.RunSubmited;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static io.seqera.tower.cli.utils.FilesHelper.readString;
import static io.seqera.tower.cli.utils.ModelHelper.coalesce;
import static io.seqera.tower.cli.utils.ModelHelper.createLaunchRequest;

@Command(
        name = "launch",
        description = "Run a Nextflow pipeline"
)
public class LaunchesCmd extends AbstractRootCmd {

    @Parameters(index = "0", paramLabel = "PIPELINE_OR_URL", description = "Workspace pipeline name or full pipeline URL", arity = "1")
    String pipeline;

    @CommandLine.Mixin
    public WorkspaceOptions workspace;

    @Option(names = {"--params"}, description = "Parameters file")
    Path params;

    @Option(names = {"-c", "--compute-env"}, description = "Compute environment name (defaults to workspace primary)")
    String computeEnv;

    @Option(names = {"-k", "--work-dir"}, description = "Working directory")
    String workDir;

    @Option(names = {"-p", "--profile"}, split = ",", description = "Configuration profiles")
    List<String> profile;

    @Option(names = {"-r", "--revision"}, description = "A valid repository commit Id, tag or branch name")
    String revision;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    AdvancedOptions adv;

    public LaunchesCmd() {
    }

    @Override
    protected Response exec() throws ApiException, IOException {

        // If the pipeline has at least one backslash consider it an external pipeline.
        if (pipeline.contains("/")) {
            return runNextflowPipeline();
        }

        // Otherwise run pipelines defined at current workspace
        return runTowerPipeline();

    }

    protected Response runNextflowPipeline() throws ApiException, IOException {

        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnv ce = computeEnv != null ? computeEnvByName(workspace.workspaceId, computeEnv) : primaryComputeEnv(workspace.workspaceId);

        return submitWorkflow(updateLaunchRequest(new WorkflowLaunchRequest()
                .pipeline(pipeline)
                .computeEnvId(ce.getId())
                .workDir(ce.getConfig().getWorkDir())
        ));
    }

    private WorkflowLaunchRequest updateLaunchRequest(WorkflowLaunchRequest base) throws IOException {
        return new WorkflowLaunchRequest()
                .pipeline(base.getPipeline())
                .computeEnvId(base.getComputeEnvId())
                .workDir(coalesce(workDir, base.getWorkDir()))
                .paramsText(coalesce(readString(params), base.getParamsText()))
                .configProfiles(coalesce(profile, base.getConfigProfiles()))
                .revision(coalesce(revision, base.getRevision()))
                .configText(coalesce(readString(adv().config), base.getConfigText()))
                .preRunScript(coalesce(readString(adv().preRunScript), base.getPreRunScript()))
                .postRunScript(coalesce(readString(adv().postRunScript), base.getPostRunScript()))
                .pullLatest(coalesce(adv().pullLatest, base.getPullLatest()))
                .stubRun(coalesce(adv().stubRun, base.getStubRun()))
                .mainScript(coalesce(adv().mainScript, base.getMainScript()))
                .entryName(coalesce(adv().entryName, base.getEntryName()))
                .schemaName(coalesce(adv().schemaName, base.getSchemaName()));
    }

    protected Response runTowerPipeline() throws ApiException, IOException {
        ListPipelinesResponse pipelines = api().listPipelines(workspace.workspaceId, 2, 0, pipeline);
        if (pipelines.getTotalSize() == 0) {
            throw new InvalidResponseException(String.format("Pipeline '%s' not found on this workspace.", pipeline));
        }

        if (pipelines.getTotalSize() > 1) {
            throw new InvalidResponseException(String.format("Multiple pipelines match '%s'", pipeline));
        }

        Long pipelineId = pipelines.getPipelines().get(0).getPipelineId();
        Launch launch = api().describePipelineLaunch(pipelineId, workspace.workspaceId).getLaunch();

        return submitWorkflow(updateLaunchRequest(createLaunchRequest(launch)));
    }

    protected Response submitWorkflow(WorkflowLaunchRequest launch) throws ApiException {
        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(new SubmitWorkflowLaunchRequest().launch(launch), workspace.workspaceId);
        String workflowId = response.getWorkflowId();
        return new RunSubmited(workflowId, workflowWatchUrl(workflowId), workspaceRef(workspace.workspaceId));
    }

    private String workflowWatchUrl(String workflowId) throws ApiException {

        if (workspace.workspaceId == null) {
            return String.format("%s/user/%s/watch/%s", serverUrl(), userName(), workflowId);
        }

        return String.format("%s/orgs/%s/workspaces/%s/watch/%s", serverUrl(), orgName(workspace.workspaceId), workspaceName(workspace.workspaceId), workflowId);
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {

        @Option(names = {"--config"}, description = "Additional Nextflow config settings can be provided in the above field. These settings will be included in the nextflow.config file for this execution")
        public Path config;

        @Option(names = {"--pre-run"}, description = "Pre-run script")
        public Path preRunScript;

        @Option(names = {"--post-run"}, description = "Post-run script")
        public Path postRunScript;

        @Option(names = {"--pull-latest"}, description = "Enabling this option Nextflow pulls the latest version from the Git repository before run the pipeline")
        public Boolean pullLatest;

        @Option(names = {"--stub-run"}, description = "Execute the workflow replacing process scripts with command stubs")
        public Boolean stubRun;

        @Option(names = {"--main-script"}, description = "Specify the pipeline main script file if different from `main.nf`")
        public String mainScript;

        @Option(names = {"--entry-name"}, description = "Specify the main workflow name to be executed when using DLS2 syntax")
        public String entryName;

        @Option(names = {"--schema-name"}, description = "Enter schema name")
        public String schemaName;

    }

}


