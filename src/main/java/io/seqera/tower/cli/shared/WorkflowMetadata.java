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

package io.seqera.tower.cli.shared;

import io.seqera.tower.cli.commands.runs.DumpCmd;
import io.seqera.tower.model.LabelDbDto;

import java.util.List;

/**
 * This is the class used by {@link DumpCmd} to structure Workflow metadata as JSON.
 */
public final class WorkflowMetadata {

    private final Long pipelineId;

    private final Long organizationId;

    private final String organizationName;

    private final Long workspaceId;

    private final String workspaceName;

    private final Long userId;

    private final String runUrl;

    private final List<LabelDbDto> labels;

    public WorkflowMetadata() {
        pipelineId = null;
        organizationId = null;
        organizationName = null;
        workspaceId = null;
        workspaceName = null;
        userId = null;
        runUrl = null;
        labels = null;
    }

    public WorkflowMetadata(
            final Long pipelineId,
            final Long organizationId,
            final String organizationName,
            final Long workspaceId,
            final String workspaceName,
            final Long userId,
            final String runUrl,
            final List<LabelDbDto> labels
    ) {
        this.pipelineId = pipelineId;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.userId = userId;
        this.runUrl = runUrl;
        this.labels = labels;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRunUrl() {
        return runUrl;
    }

    public List<LabelDbDto> getLabels() {
        return labels;
    }
}
