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

package io.seqera.tower.cli.shared;

import io.seqera.tower.cli.commands.runs.DumpCmd;

/**
 * This is the class used by {@link DumpCmd} to structure Workflow metadata as JSON.
 */
public final class WorkflowMetadata {

    private final Long pipelineId;

    private final Long workspaceId;

    private final Long userId;

    private final String userEmail;

    public WorkflowMetadata(
            final Long pipelineId,
            final Long workspaceId,
            final Long userId,
            final String userEmail
    ) {
        this.pipelineId = pipelineId;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.userEmail = userEmail;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

}
