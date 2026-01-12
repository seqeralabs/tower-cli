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

package io.seqera.tower.cli.commands;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.labels.Label;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunSubmited;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import io.seqera.tower.model.WorkflowStatus;
import jakarta.annotation.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static io.seqera.tower.cli.utils.FilesHelper.readString;
import static io.seqera.tower.cli.utils.ModelHelper.coalesce;
import static io.seqera.tower.cli.utils.ModelHelper.createLaunchRequest;
import static io.seqera.tower.cli.utils.ModelHelper.removeEmptyValues;
import static io.seqera.tower.cli.utils.ResponseHelper.waitStatus;

@Command(
        name = "launch",
        description = "Launch a Nextflow pipeline execution."
)
public class LaunchCmd extends AbstractRootCmd {

    @Parameters(index = "0", paramLabel = "PIPELINE_OR_URL", description = "Workspace pipeline name or full pipeline URL.", arity = "1")
    String pipeline;

    @CommandLine.Mixin
    public WorkspaceOptionalOptions workspace;

    @Option(names = {"--params-file"}, description = "Pipeline parameters in either JSON or YML format.")
    Path paramsFile;

    @Option(names = {"-c", "--compute-env"}, description = "Compute environment name [default: primary compute environment].")
    String computeEnv;

    @Option(names = {"-n", "--name"}, description = "Custom workflow run name")
    String name;

    @Option(names = {"--work-dir"}, description = "Path where the pipeline scratch data is stored.")
    String workDir;

    @Option(names = {"-p", "--profile"}, split = ",", description = "Comma-separated list of one or more configuration profile names you want to use for this pipeline execution.")
    List<String> profile;

    @Option(names = {"-r", "--revision"}, description = "A valid repository commit Id, tag or branch name.")
    String revision;

    @Option(names = {"--wait"}, description = "Wait until given status or fail. Valid options: ${COMPLETION-CANDIDATES}.")
    public WorkflowStatus wait;

    @Option(names = {"-l", "--labels"}, split = ",", description = "Comma-separated list of labels for the pipeline. Use 'key=value' format for resource labels.", converter = Label.LabelConverter.class)
    List<Label> labels;

    @Option(names = {"--launch-container"}, description = "Container to be used to run the nextflow head job (BETA).")
    String launchContainer;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    AdvancedOptions adv;

    public LaunchCmd() {
    }

    @Override
    protected Response exec() throws ApiException, IOException {
        Long wspId = workspaceId(workspace.workspace);

        // If the pipeline has at least one backslash consider it an external pipeline.
        if (pipeline.startsWith("https://") || pipeline.startsWith("http://") || pipeline.startsWith("file:/")) {
            return runNextflowPipeline(wspId);
        }

        // Otherwise run pipelines defined at current workspace
        return runTowerPipeline(wspId);

    }

    protected Response runNextflowPipeline(Long wspId) throws ApiException, IOException {
        // Retrieve the provided computeEnv or use the primary if not provided
        ComputeEnvResponseDto ce = computeEnv != null ? computeEnvByRef(wspId, computeEnv) : primaryComputeEnv(wspId);
        // Retrieve the IDs for the labels specified by the user if any
        List<Long> labels = obtainLabelIDs(wspId);

        return submitWorkflow(updateLaunchRequest(new WorkflowLaunchRequest()
                .pipeline(pipeline)
                .labelIds(labels.isEmpty() ? null : labels)
                .computeEnvId(ce.getId())
                .workDir(ce.getConfig().getWorkDir())
                .preRunScript(ce.getConfig().getPreRunScript())
                .postRunScript(ce.getConfig().getPostRunScript())
        ), wspId, null);
    }

    private WorkflowLaunchRequest updateLaunchRequest(WorkflowLaunchRequest base) throws IOException {
        return new WorkflowLaunchRequest()
                .id(base.getId())
                .computeEnvId(base.getComputeEnvId())
                .runName(coalesce(name, base.getRunName()))
                .pipeline(base.getPipeline())
                .workDir(coalesce(workDir, base.getWorkDir()))
                .revision(coalesce(revision, base.getRevision()))
                .configProfiles(coalesce(profile, base.getConfigProfiles()))
                .userSecrets(coalesce(removeEmptyValues(adv().userSecrets), base.getUserSecrets()))
                .workspaceSecrets(coalesce(removeEmptyValues(adv().workspaceSecrets), base.getWorkspaceSecrets()))
                .configText(coalesce(readString(adv().config), base.getConfigText()))
                .towerConfig(base.getTowerConfig())
                .paramsText(coalesce(readString(paramsFile), base.getParamsText()))
                .preRunScript(coalesce(readString(adv().preRunScript), base.getPreRunScript()))
                .postRunScript(coalesce(readString(adv().postRunScript), base.getPostRunScript()))
                .mainScript(coalesce(adv().mainScript, base.getMainScript()))
                .entryName(coalesce(adv().entryName, base.getEntryName()))
                .schemaName(coalesce(adv().schemaName, base.getSchemaName()))
                .pullLatest(coalesce(adv().pullLatest, base.getPullLatest()))
                .stubRun(coalesce(adv().stubRun, base.getStubRun()))
                .optimizationId(coalesce(adv().disableOptimization, false) ? null : base.getOptimizationId())
                .optimizationTargets(coalesce(adv().disableOptimization, false) ? null : base.getOptimizationTargets())
                .labelIds(base.getLabelIds())
                .headJobCpus(coalesce(adv().headJobCpus, base.getHeadJobCpus()))
                .headJobMemoryMb(coalesce(adv().headJobMemoryMb, base.getHeadJobMemoryMb()))
                .launchContainer(launchContainer);
    }

    protected Response runTowerPipeline(Long wspId) throws ApiException, IOException {

        ListPipelinesResponse pipelines = pipelinesApi().listPipelines(Collections.emptyList(), wspId, 50, 0, null, null, pipeline, "all");
        if (pipelines.getTotalSize() == 0) {
            throw new InvalidResponseException(String.format("Pipeline '%s' not found on this workspace.", pipeline));
        }

        PipelineDbDto pipe = null;
        for (PipelineDbDto p : pipelines.getPipelines()) {
            if (pipeline.equals(p.getName())) {
                pipe = p;
                break;
            }
        }

        if (pipe == null) {
            throw new InvalidResponseException(String.format("Pipeline '%s' not found", pipeline));
        }

        Long sourceWorkspaceId = sourceWorkspaceId(wspId, pipe);

        Launch launch = pipelinesApi().describePipelineLaunch(pipe.getPipelineId(), wspId, sourceWorkspaceId).getLaunch();

        WorkflowLaunchRequest launchRequest = createLaunchRequest(launch);
        if (computeEnv != null) {
            ComputeEnvResponseDto ce = computeEnvByRef(wspId, computeEnv);
            launchRequest.computeEnvId(ce.getId());
            launchRequest.workDir(ce.getConfig().getWorkDir());
            launchRequest.preRunScript( coalesce(launchRequest.getPreRunScript(), ce.getConfig().getPreRunScript()) );
            launchRequest.postRunScript( coalesce(launchRequest.getPostRunScript(), ce.getConfig().getPostRunScript()) );
            launchRequest.configText( coalesce(launchRequest.getConfigText(), ce.getConfig().getNextflowConfig()) );
        }

        if (launchRequest.getComputeEnvId() == null) {
            launchRequest.computeEnvId(primaryComputeEnv(wspId).getId());
        }

        if (launchRequest.getWorkDir() == null) {
            ComputeEnvResponseDto ce = computeEnvsApi().describeComputeEnv(launchRequest.getComputeEnvId(), wspId, NO_CE_ATTRIBUTES).getComputeEnv();
            launchRequest.workDir(ce.getConfig().getWorkDir());
        }

        List<Long> labels = obtainLabelIDs(wspId);
        launchRequest.labelIds(labels.isEmpty() ? null : labels);

        return submitWorkflow(updateLaunchRequest(launchRequest), wspId, sourceWorkspaceId);
    }

    protected Response submitWorkflow(WorkflowLaunchRequest launch, Long wspId, Long sourceWorkspaceId) throws ApiException {
        SubmitWorkflowLaunchResponse response = workflowsApi().createWorkflowLaunch(new SubmitWorkflowLaunchRequest().launch(launch), wspId, sourceWorkspaceId);
        String workflowId = response.getWorkflowId();
        return new RunSubmited(workflowId, wspId, baseWorkspaceUrl(wspId), workspaceRef(wspId));
    }

    @Override
    protected Integer onBeforeExit(int exitCode, Response response) {

        if (exitCode != 0 || wait == null || response == null) {
            return exitCode;
        }

        RunSubmited submitted = (RunSubmited) response;
        boolean showProgress = app().output != OutputType.json;

        try {
            return waitStatus(
                    app().getOut(),
                    showProgress,
                    wait,
                    WorkflowStatus.values(),
                    () -> checkWorkflowStatus(submitted.workflowId, submitted.workspaceId),
                    WorkflowStatus.CANCELLED, WorkflowStatus.FAILED, WorkflowStatus.SUCCEEDED
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return exitCode;
        }
    }

    private WorkflowStatus checkWorkflowStatus(String workflowId, Long workspaceId) {
        try {
            return workflowsApi().describeWorkflow(workflowId, workspaceId, NO_WORKFLOW_ATTRIBUTES).getWorkflow().getStatus();
        } catch (ApiException | NullPointerException e) {
            return null;
        }
    }

    private List<Long> obtainLabelIDs(@Nullable Long workspaceId) throws ApiException {

        if (labels == null || labels.isEmpty()) {
            return Collections.emptyList();
        }
        return findOrCreateLabels(workspaceId, labels);
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {

        @Option(names = {"--config"}, description = "Additional Nextflow config file.")
        public Path config;

        @Option(names = {"--pre-run"}, description = "Bash script that is executed in the same environment where Nextflow runs just before the pipeline is launched.")
        public Path preRunScript;

        @Option(names = {"--post-run"}, description = "Bash script that is executed in the same environment where Nextflow runs immediately after the pipeline completion.")
        public Path postRunScript;

        @Option(names = {"--pull-latest"}, description = "Enable Nextflow to pull the latest repository version before running the pipeline.")
        public Boolean pullLatest;

        @Option(names = {"--stub-run"}, description = "Execute the workflow replacing process scripts with command stubs.")
        public Boolean stubRun;

        @Option(names = {"--main-script"}, description = "Pipeline main script file if different from `main.nf`.")
        public String mainScript;

        @Option(names = {"--entry-name"}, description = "Main workflow name to be executed when using DLS2 syntax.")
        public String entryName;

        @Option(names = {"--schema-name"}, description = "Schema name.")
        public String schemaName;

        @Option(names = {"--user-secrets"}, split = ",", description = "Pipeline Secrets required by the pipeline execution that belong to the launching user personal context. User's secrets will take precedence over workspace secrets with the same name.")
        public List<String> userSecrets;

        @Option(names = {"--workspace-secrets"}, split = ",", description = "Pipeline Secrets required by the pipeline execution. Those secrets must be defined in the launching workspace.")
        public List<String> workspaceSecrets;

        @Option(names = {"--disable-optimization"}, description = "Turn off the optimization for the pipeline before launching.")
        public Boolean disableOptimization;

        @Option(names = {"--head-job-cpus"}, description = "The number of CPUs to be allocated for the Nextflow runner job (overrides compute environment setting).")
        public Integer headJobCpus;

        @Option(names = {"--head-job-memory"}, description = "The number of MiB of memory reserved for the Nextflow runner job (overrides compute environment setting).")
        public Integer headJobMemoryMb;

    }

}


