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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.ComputeEnvsApi;
import io.seqera.tower.cli.exceptions.InvalidResponseException;
import io.seqera.tower.model.ComputeEnvComputeConfig;
import io.seqera.tower.model.ComputeEnvQueryAttribute;
import io.seqera.tower.model.ComputeEnvResponseDto;
import io.seqera.tower.model.ComputeEnvStatus;
import io.seqera.tower.model.CreateComputeEnvRequest;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import io.seqera.tower.model.UpdateComputeEnvRequest;
import io.seqera.tower.model.ValidateComputeEnvResponse;
import io.seqera.tower.v2.api.ComputeEnvironmentsApi;
import io.seqera.tower.v2.model.ComputeEnvPatchModel;
import io.seqera.tower.v2.model.ComputeEnvReadModel;
import io.seqera.tower.v2.model.ComputeEnvWriteModel;
import io.seqera.tower.v2.model.ComputeEnvsPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute-environment calls. Workspace-scoped calls go through the Platform API v2
 * ({@code /v2/workspaces/{workspaceId}/compute-envs}). The v2 contract has no route for the
 * personal (user) workspace, so calls without a workspace stay on the v1 API.
 */
public class ComputeEnvsClient {

    private static final int V2_PAGE_SIZE = 100;
    private static final List<String> V2_INCLUDE_LABELS = List.of("resourceLabels");

    private final ComputeEnvsApi v1;
    private final ComputeEnvironmentsApi v2;

    public ComputeEnvsClient(ComputeEnvsApi v1, ComputeEnvironmentsApi v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @FunctionalInterface
    private interface V2Call<T> {
        T call() throws io.seqera.tower.v2.ApiException;
    }

    /** Surfaces v2 client errors as the API errors the CLI already reports. */
    private static <T> T v2(V2Call<T> call) throws ApiException {
        try {
            return call.call();
        } catch (io.seqera.tower.v2.ApiException e) {
            throw new ApiException(e.getMessage(), e.getCode(), e.getResponseHeaders(), e.getResponseBody());
        }
    }

    private static String wsp(Long workspaceId) {
        return workspaceId.toString();
    }

    public ComputeEnvResponseDto describe(Long workspaceId, String computeEnvId, boolean withLabels) throws ApiException {
        if (workspaceId == null) {
            List<ComputeEnvQueryAttribute> attributes = withLabels ? List.of(ComputeEnvQueryAttribute.labels) : Collections.emptyList();
            return v1.describeComputeEnv(computeEnvId, null, attributes).getComputeEnv();
        }
        ComputeEnvReadModel ce = v2(() -> v2.getComputeEnv(wsp(workspaceId), computeEnvId, withLabels ? V2_INCLUDE_LABELS : null));
        return ComputeEnvV2Mapper.toV1(ce, workspaceId);
    }

    /**
     * Lists the compute environments of the workspace, optionally only those with the given status.
     */
    public List<ListComputeEnvsResponseEntry> list(Long workspaceId, ComputeEnvStatus status) throws ApiException {
        if (workspaceId == null) {
            return v1.listComputeEnvs(status == null ? null : status.getValue(), null, List.of()).getComputeEnvs();
        }
        String filter = status == null ? null : String.format("status == \"%s\"", status.getValue());
        List<ListComputeEnvsResponseEntry> result = new ArrayList<>();
        String pageToken = null;
        do {
            String token = pageToken;
            ComputeEnvsPage page = v2(() -> v2.listComputeEnvs(wsp(workspaceId), token, V2_PAGE_SIZE, filter, null));
            result.addAll(page.getItems().stream().map(ComputeEnvV2Mapper::toV1ListEntry).collect(Collectors.toList()));
            pageToken = page.getNextPageToken();
        } while (pageToken != null);
        return result;
    }

    /**
     * Creates the compute environment and returns its identifier.
     */
    public String create(Long workspaceId, ComputeEnvComputeConfig computeEnv, List<Long> labelIds) throws ApiException {
        if (workspaceId == null) {
            CreateComputeEnvRequest request = new CreateComputeEnvRequest().computeEnv(computeEnv).labelIds(labelIds);
            return v1.createComputeEnv(request, null).getComputeEnvId();
        }
        ComputeEnvWriteModel request = new ComputeEnvWriteModel()
                .name(computeEnv.getName())
                .description(computeEnv.getDescription())
                .credentialsId(computeEnv.getCredentialsId())
                .fusionMetricsCollectionEnabled(computeEnv.getFusionMetricsCollectionEnabled())
                .resourceLabelIds(labelIds == null ? null : labelIds.stream().map(String::valueOf).collect(Collectors.toList()))
                .config(ComputeEnvV2Mapper.toV2Config(computeEnv.getConfig()));
        return v2(() -> v2.createComputeEnv(wsp(workspaceId), request, null)).getId();
    }

    /**
     * Renames the compute environment and/or replaces its description; a {@code null} argument leaves it unchanged.
     */
    public void update(Long workspaceId, ComputeEnvResponseDto current, String newName, String description) throws ApiException {
        if (workspaceId == null) {
            // v1 only (tower-cli:rule-2, tower-cli:rule-3): pre-validate the name, then PUT the full name/description
            if (newName != null) {
                try {
                    v1.validateComputeEnvName(null, newName);
                } catch (ApiException ex) {
                    throw new InvalidResponseException(String.format("Compute environment name '%s' is not valid", newName));
                }
            }
            UpdateComputeEnvRequest req = new UpdateComputeEnvRequest()
                    .name(newName != null ? newName : current.getName())
                    .description(description != null ? description : current.getDescription());
            v1.updateComputeEnv(current.getId(), req, null);
            return;
        }
        // tower-cli:rule-2 and tower-cli:rule-3 deleted for v2: PATCH writes only the fields carried in the body
        // (no coalescing with current values), validates the name pattern and rejects a duplicate name with 409.
        ComputeEnvPatchModel patch = new ComputeEnvPatchModel().name(newName).description(description);
        v2(() -> v2.updateComputeEnv(wsp(workspaceId), current.getId(), patch, null, null));
    }

    public void delete(Long workspaceId, String computeEnvId) throws ApiException {
        if (workspaceId == null) {
            v1.deleteComputeEnv(computeEnvId, null, null);
            return;
        }
        v2(() -> {
            v2.deleteComputeEnv(wsp(workspaceId), computeEnvId, null);
            return null;
        });
    }

    /**
     * Current status of the compute environment. A v2 compute environment that has been deleted can no
     * longer be read (403), which is reported as {@link ComputeEnvStatus#DELETED}.
     */
    public ComputeEnvStatus status(Long workspaceId, String computeEnvId) throws ApiException {
        if (workspaceId == null) {
            return v1.describeComputeEnv(computeEnvId, null, Collections.emptyList()).getComputeEnv().getStatus();
        }
        try {
            return ComputeEnvV2Mapper.toV1(v2(() -> v2.getComputeEnv(wsp(workspaceId), computeEnvId, null)).getStatus());
        } catch (ApiException e) {
            // KEPT: no server equivalent (tower-cli:rule-20) - added for v2, which has no DELETED status and rejects
            // reading a deleted compute environment with 403
            if (e.getCode() == 403) {
                return ComputeEnvStatus.DELETED;
            }
            throw e;
        }
    }

    public void enable(Long workspaceId, String computeEnvId) throws ApiException {
        if (workspaceId == null) {
            v1.enableComputeEnv(computeEnvId, null, null);
            return;
        }
        v2(() -> {
            v2.enableComputeEnv(wsp(workspaceId), computeEnvId);
            return null;
        });
    }

    public void disable(Long workspaceId, String computeEnvId) throws ApiException {
        if (workspaceId == null) {
            v1.disableComputeEnv(computeEnvId, null, null);
            return;
        }
        v2(() -> {
            v2.disableComputeEnv(wsp(workspaceId), computeEnvId);
            return null;
        });
    }

    public void setPrimary(Long workspaceId, String computeEnvId) throws ApiException {
        if (workspaceId == null) {
            v1.updateComputeEnvPrimary(computeEnvId, null, null);
            return;
        }
        v2(() -> {
            v2.setPrimaryComputeEnv(wsp(workspaceId), computeEnvId);
            return null;
        });
    }

    public ValidateComputeEnvResponse validate(Long workspaceId, String computeEnvId, boolean force) throws ApiException {
        if (workspaceId == null) {
            return v1.validateComputeEnv(computeEnvId, null, force, null);
        }
        io.seqera.tower.v2.model.ValidateComputeEnvResponse response = v2(() -> v2.validateComputeEnv(wsp(workspaceId), computeEnvId, force));
        return new ValidateComputeEnvResponse()
                .status(ComputeEnvV2Mapper.toV1(response.getStatus()))
                .message(response.getMessage())
                .transientError(response.getTransientError());
    }
}
