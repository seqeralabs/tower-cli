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

package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.global.WorkspaceOptionalOptions;
import io.seqera.tower.cli.commands.pipelines.versions.VersionRefOptions;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.responses.pipelines.PipelinesUpdated;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.cli.utils.VersionNameHelper;
import io.seqera.tower.model.LaunchDbDto;
import io.seqera.tower.model.ListPipelineVersionsResponse;
import io.seqera.tower.model.PipelineDbDto;
import io.seqera.tower.model.PipelineVersionFullInfoDto;
import io.seqera.tower.model.PipelineVersionManageRequest;
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

    @Option(names = {"--allow-draft"}, description = "If versionable fields change, keep the new version as an unnamed draft instead of auto-naming and promoting it to default.")
    public boolean allowDraft;

    // Explicit "0..1" for clarity — contrasts with the required "1" in VersionRefOptions. @Mixin won't work here as it would lose mutual exclusivity.
    @CommandLine.ArgGroup(multiplicity = "0..1")
    public VersionRefOptions.VersionRef versionRef;

    @Override
    protected Response exec() throws ApiException, IOException {

        Long wspId = workspaceId(workspace.workspace);

        // Resolve the pipeline by --id or --name so we have its metadata (name, description, etc.)
        PipelineDbDto pipe = fetchPipeline(wspId);

        // If the user wants to rename the pipeline (--new-name), validate early before making any
        // changes. The server checks uniqueness within the workspace/org scope.
        validateNewName(newName, wspId);

        // Determine which version to update. If the user passed --version-id or --version-name, we
        // target that specific version; otherwise we target the pipeline's current default version.
        // We also eagerly fetch the default version's name here — it's needed later to derive the
        // auto-generated name if a new draft version is created by the server.
        VersionTarget target = resolveVersionTarget(pipe, wspId);

        // Fetch the existing launch configuration for the target version so we can merge the user's
        // CLI overrides on top of the current values (coalesce pattern: user value wins, else keep existing).
        LaunchDbDto launch = fetchLaunch(pipe, wspId, target.versionId);

        // Resolve the compute environment: use --compute-env if provided, otherwise keep whatever
        // the launch already references. This needs a separate API call when the user specifies a
        // CE by name (to look up its ID).
        String ceId = resolveComputeEnvId(wspId, launch);

        // Build the update payload merging CLI flags with existing launch values.
        UpdatePipelineRequest updateReq = buildUpdateRequest(pipe, launch, ceId);

        // Send the update to the versioned endpoint (POST /pipelines/{id}/versions/{vid}).
        // If versionable fields (revision, pipeline URL, etc.) changed, the server may create a
        // new draft version instead of updating the existing one in-place.
        UpdatePipelineResponse response = pipelineVersionsApi()
                .updatePipelineVersion(pipe.getPipelineId(), target.versionId, updateReq, wspId);

        // Detect whether a new version was created and handle it:
        //  - Server already named it (fixed API) → just report it.
        //  - Unnamed draft + --allow-draft       → report draft ID, let user manage it manually.
        //  - Unnamed draft (default behavior)    → auto-generate a name, assign it, and promote to default
        //    (mirrors the frontend auto-naming algorithm).
        return handleVersioningResult(response, target, pipe, wspId);
    }

    // --- Pipeline resolution ---

    private PipelineDbDto fetchPipeline(Long wspId) throws ApiException {
        if (pipelineRefOptions.pipeline.pipelineId != null) {
            Long id = pipelineRefOptions.pipeline.pipelineId;
            return pipelinesApi().describePipeline(id, Collections.emptyList(), wspId, null).getPipeline();
        }
        return pipelineByName(wspId, pipelineRefOptions.pipeline.pipelineName);
    }

    private void validateNewName(String newName, Long wspId) throws ApiException {
        if (newName == null) return;
        Long orgId = wspId != null ? orgId(wspId) : null;
        try {
            pipelinesApi().validatePipelineName(wspId, orgId, newName);
        } catch (ApiException ex) {
            throw new InvalidResponseException(String.format("Pipeline name '%s' is not valid", newName));
        }
    }

    // --- Version resolution ---

    /**
     * Carries the resolved version ID to update and the current default version's name.
     * The default version name is needed later by autoNameAndPromote to derive the next
     * incremental name (e.g. "pipeline-3" → "pipeline-4").
     */
    private static class VersionTarget {
        final String versionId;
        final String defaultVersionName;

        VersionTarget(String versionId, String defaultVersionName) {
            this.versionId = versionId;
            this.defaultVersionName = defaultVersionName;
        }
    }

    private VersionTarget resolveVersionTarget(PipelineDbDto pipe, Long wspId) throws ApiException {
        // Always fetch the default version eagerly — we need its name for auto-naming if a draft
        // is created, regardless of whether we're updating the default or a user-specified version.
        // Falls back to the pipeline name if no default version exists yet.
        PipelineVersionFullInfoDto defaultVersion = fetchDefaultVersion(pipe.getPipelineId(), wspId);
        String defaultVersionName = defaultVersion != null ? defaultVersion.getName() : pipe.getName();

        if (versionRef != null) {
            // User explicitly targeted a version via --version-id or --version-name
            String versionId = resolvePipelineVersionId(pipe.getPipelineId(), wspId, versionRef);
            return new VersionTarget(versionId, defaultVersionName);
        }

        // No explicit version — target the default. Every pipeline should have one, but guard
        // against edge cases (e.g. pipeline just created, no versions published yet).
        if (defaultVersion == null) {
            throw new TowerException(String.format("No default version found for pipeline '%s'", pipe.getName()));
        }
        return new VersionTarget(defaultVersion.getId(), defaultVersionName);
    }

    // --- Launch and request building ---

    private LaunchDbDto fetchLaunch(PipelineDbDto pipe, Long wspId, String versionId) throws ApiException {
        // sourceWorkspaceId handles shared pipelines — the launch config lives in the source
        // workspace, not necessarily the workspace where the user is operating.
        Long sourceWspId = sourceWorkspaceId(wspId, pipe);
        return pipelinesApi().describePipelineLaunch(pipe.getPipelineId(), wspId, sourceWspId, versionId).getLaunch();
    }

    private String resolveComputeEnvId(Long wspId, LaunchDbDto launch) throws ApiException {
        // User explicitly picked a compute environment — resolve its ID by name or ID ref.
        if (opts.computeEnv != null) {
            return computeEnvByRef(wspId, opts.computeEnv).getId();
        }
        // Keep the existing CE from the launch; may be null for shared workspace pipelines
        // that don't have a CE pinned.
        var ce = launch.getComputeEnv();
        return ce != null ? ce.getId() : null;
    }

    /**
     * Merges CLI-provided values with the existing launch configuration.
     * Each field uses coalesce(userValue, existingValue): the user's flag wins if provided,
     * otherwise we preserve the current server-side value. This lets users update individual
     * fields without having to re-specify everything.
     */
    private UpdatePipelineRequest buildUpdateRequest(PipelineDbDto pipe, LaunchDbDto launch, String ceId) throws IOException {
        return new UpdatePipelineRequest()
                .name(coalesce(newName, pipe.getName()))
                .description(coalesce(description, pipe.getDescription()))
                .launch(new WorkflowLaunchRequest()
                        .computeEnvId(ceId)
                        .pipeline(coalesce(pipeline, launch.getPipeline()))
                        .revision(coalesce(opts.revision, launch.getRevision()))
                        .commitId(opts.commitId)
                        .workDir(coalesce(opts.workDir, launch.getWorkDir()))
                        .configProfiles(coalesce(opts.profile, launch.getConfigProfiles()))
                        .paramsText(coalesce(FilesHelper.readString(opts.paramsFile), launch.getParamsText()))
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
    }

    // --- Post-update version handling ---
    //
    // When versionable fields (revision, pipeline URL, etc.) change, the server creates a new
    // version instead of modifying the existing one in-place.
    // The new version is an unnamed draft (name=null, isDefault=false).
    // We must auto-generate a name, assign it, and promote it to default so it appears in the
    // Launchpad — unless the user passed --allow-draft to keep it as a draft.
    //
    // If no versionable fields changed, the server updates the version in-place and returns the
    // same version ID — detectNewVersion returns null and we skip all of this.

    private Response handleVersioningResult(
            UpdatePipelineResponse response, VersionTarget target, PipelineDbDto pipe, Long wspId
    ) throws ApiException {
        PipelineVersionFullInfoDto newVersion = detectNewVersion(response, target.versionId);

        String newVersionName = null;
        String draftVersionId = null;

        if (newVersion != null) {
            if (newVersion.getName() != null) {
                // Fixed API path: server already named the version and promoted it to default.
                newVersionName = newVersion.getName();
            } else if (allowDraft) {
                // User explicitly opted to keep the draft as-is for manual management via
                // 'tw pipelines versions'.
                draftVersionId = newVersion.getId();
            } else {
                // Current API path: unnamed draft. Auto-name it following the frontend convention
                // (e.g. "pipeline-3" → "pipeline-4") and promote it to default so it's immediately
                // available in the Launchpad.
                newVersionName = autoNameAndPromote(newVersion, target, pipe, wspId);
            }
        }

        return new PipelinesUpdated(workspaceRef(wspId), pipe.getName(), newVersionName, draftVersionId);
    }

    /**
     * Names an unnamed draft version and promotes it to default. This mirrors the frontend's
     * behavior: derive an incremental name from the current default
     * version name, validate it against the server (to avoid collisions), and assign it.
     */
    private String autoNameAndPromote(
            PipelineVersionFullInfoDto newVersion, VersionTarget target, PipelineDbDto pipe, Long wspId
    ) throws ApiException {
        String versionName = VersionNameHelper.generateValidVersionName(
                target.defaultVersionName, pipe.getPipelineId(), wspId, pipelineVersionsApi()
        );

        pipelineVersionsApi().managePipelineVersion(
                pipe.getPipelineId(),
                newVersion.getId(),
                new PipelineVersionManageRequest().name(versionName).isDefault(true),
                wspId
        );

        return versionName;
    }

    /**
     * Detects whether the server created a new version as a side effect of the update.
     * If the response contains a version with a different ID than the one we sent the update to,
     * that's a newly created version (either named or draft). Same ID means in-place update.
     */
    private PipelineVersionFullInfoDto detectNewVersion(UpdatePipelineResponse response, String requestedVersionId) {
        if (response == null || response.getPipeline() == null) return null;
        PipelineVersionFullInfoDto version = response.getPipeline().getVersion();
        if (version == null) return null;
        return version.getId().equals(requestedVersionId) ? null : version;
    }

    private PipelineVersionFullInfoDto fetchDefaultVersion(Long pipelineId, Long wspId) throws ApiException {
        // Query published versions and find the one marked as default. We use the published filter
        // because draft versions should not be considered as the "current default" for naming.
        ListPipelineVersionsResponse versionsResponse = pipelineVersionsApi()
                .listPipelineVersions(pipelineId, wspId, null, null, null, true);
        if (versionsResponse.getVersions() == null) return null;
        return versionsResponse.getVersions().stream()
                .map(PipelineDbDto::getVersion)
                .filter(v -> v != null && Boolean.TRUE.equals(v.getIsDefault()))
                .findFirst()
                .orElse(null);
    }
}
