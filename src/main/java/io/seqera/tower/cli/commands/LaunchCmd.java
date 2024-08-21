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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.runs.RunSubmited;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.CreateLabelRequest;
import io.seqera.tower.model.CreateLabelResponse;
import io.seqera.tower.model.LabelDbDto;
import io.seqera.tower.model.LabelType;
import io.seqera.tower.model.Launch;
import io.seqera.tower.model.ListLabelsResponse;
import io.seqera.tower.model.ListPipelinesResponse;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.SubmitWorkflowLaunchRequest;
import io.seqera.tower.model.SubmitWorkflowLaunchResponse;
import io.seqera.tower.model.WorkflowLaunchRequest;
import io.seqera.tower.model.WorkflowStatus;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    @Option(names = {"-l", "--labels"}, split = ",", description = "Comma-separated list of labels for the pipeline.")
    List<String> labels;

    @Option(names = {"--launch-container"}, split = ",", description = "Container to be used to run the nextflow head job.")
    List<String> launchContainer;

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
                .headJobCpus(base.getHeadJobCpus())
                .headJobMemoryMb(base.getHeadJobMemoryMb()
                .launchContainer(coalesce(launchContainer, base.getLaunchContainer())));
    }

    protected Response runTowerPipeline(Long wspId) throws ApiException, IOException {

        ListPipelinesResponse pipelines = api().listPipelines(Collections.emptyList(), wspId, 50, 0, pipeline, "all");
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

        Launch launch = api().describePipelineLaunch(pipe.getPipelineId(), wspId, sourceWorkspaceId).getLaunch();

        WorkflowLaunchRequest launchRequest = createLaunchRequest(launch);
        if (computeEnv != null) {
            ComputeEnvResponseDto ce = computeEnvByRef(wspId, computeEnv);
            launchRequest.computeEnvId(ce.getId());
            launchRequest.workDir(ce.getConfig().getWorkDir());
        }

        if (launchRequest.getComputeEnvId() == null) {
            launchRequest.computeEnvId(primaryComputeEnv(wspId).getId());
        }

        if (launchRequest.getWorkDir() == null) {
            ComputeEnvResponseDto ce = api().describeComputeEnv(launchRequest.getComputeEnvId(), wspId, NO_CE_ATTRIBUTES).getComputeEnv();
            launchRequest.workDir(ce.getConfig().getWorkDir());
        }

        List<Long> labels = obtainLabelIDs(wspId);
        launchRequest.labelIds(labels.isEmpty() ? null : labels);

        return submitWorkflow(updateLaunchRequest(launchRequest), wspId, sourceWorkspaceId);
    }

    protected Response submitWorkflow(WorkflowLaunchRequest launch, Long wspId, Long sourceWorkspaceId) throws ApiException {
        SubmitWorkflowLaunchResponse response = api().createWorkflowLaunch(new SubmitWorkflowLaunchRequest().launch(launch), wspId, sourceWorkspaceId);
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
            return exitCode;
        }
    }

    private WorkflowStatus checkWorkflowStatus(String workflowId, Long workspaceId) {
        try {
            return api().describeWorkflow(workflowId, workspaceId, NO_WORKFLOW_ATTRIBUTES).getWorkflow().getStatus();
        } catch (ApiException | NullPointerException e) {
            return null;
        }
    }

    private List<Long> obtainLabelIDs(@Nullable Long workspaceId) throws ApiException {

        if (labels == null || labels.isEmpty()) {
            return Collections.emptyList();
        }

        // retrieve labels for the workspace and check if we need to create new ones
        List<LabelDbDto> wspLabels = new ArrayList<>();

        ListLabelsResponse res = api().listLabels(workspaceId, null, null, null, LabelType.SIMPLE, null);
        if (res.getLabels() != null) {
            wspLabels.addAll(res.getLabels());
        }

        Map<String, Long> nameToID = wspLabels
            .stream()
            .collect(Collectors.toMap(LabelDbDto::getName, LabelDbDto::getId));

        // get label names not registered in workspace (names are unique per wspID)
        List<String> newLabels = labels
            .stream()
            .filter(labelName -> !nameToID.containsKey(labelName))
            .collect(Collectors.toList());

        if (!newLabels.isEmpty() && !labelPermission(workspaceId)) {
            throw new ApiException("User does not have permission to modify pipeline labels");
        }

        // create the new ones via POST /labels
        for (String labelName: newLabels) {
            CreateLabelResponse created = api().createLabel(
                new CreateLabelRequest()
                    .name(labelName)
                    .resource(false)
                    .isDefault(false),
                workspaceId
            );
            nameToID.put(created.getName(), created.getId());
        }

        // map requested label names to label IDs
        return labels
            .stream()
            .map(nameToID::get)
            .collect(Collectors.toList());
    }

    private boolean labelPermission(@Nullable Long wspId) throws ApiException {

        // personal workspace
        if (wspId == null) return true;

        var client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        var uri = UriBuilder
            .fromUri(URI.create(apiUrl() + "/permissions"))
            .queryParam("workspaceId", wspId.toString())
            .build();

        var req = HttpRequest.newBuilder()
            .GET()
            .uri(uri)
            .header("Authorization", String.format("Bearer %s", token()))
            .build();

        try {
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString()); // sync

            JsonNode json = new ObjectMapper().readTree(response.body());

            var roleSet = new HashSet<String>();

            json.get("workspace").get("roles").forEach(role -> roleSet.add(role.textValue()));

            return roleSet.contains("owner") || roleSet.contains("admin") || roleSet.contains("maintain");

        } catch (Throwable exception) {
            throw new ApiException("Unable to reach API");
        }
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

    }

}


