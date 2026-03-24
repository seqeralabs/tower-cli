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

package io.seqera.tower.cli.responses.pipelines.versions;

import io.seqera.tower.cli.responses.Response;

public class ManagePipelineVersionCmdResponse extends Response {

    public final String workspaceRef;
    public final Long pipelineId;
    public final String pipelineName;
    public final String versionId;

    public ManagePipelineVersionCmdResponse(String workspaceRef, Long pipelineId, String pipelineName, String versionId) {
        this.workspaceRef = workspaceRef;
        this.pipelineId = pipelineId;
        this.pipelineName = pipelineName;
        this.versionId = versionId;
    }

    @Override
    public String toString() {
        if (workspaceRef != null) {
            return ansi(String.format("%n  @|yellow Pipeline version '%s' of pipeline '%s' updated at workspace %s|@%n", versionId, pipelineName, workspaceRef));
        }
        return ansi(String.format("%n  @|yellow Pipeline version '%s' of pipeline '%s' updated|@%n", versionId, pipelineName));
    }
}
