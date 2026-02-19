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

package io.seqera.tower.cli.responses.pipelines;

import io.seqera.tower.cli.responses.Response;
import jakarta.annotation.Nullable;

public class PipelinesUpdated extends Response {

    public final String workspaceRef;
    public final String pipelineName;
    @Nullable
    public final String draftVersionId;

    public PipelinesUpdated(String workspaceRef, String pipelineName) {
        this(workspaceRef, pipelineName, null);
    }

    public PipelinesUpdated(String workspaceRef, String pipelineName, @Nullable String draftVersionId) {
        this.workspaceRef = workspaceRef;
        this.pipelineName = pipelineName;
        this.draftVersionId = draftVersionId;
    }

    @Override
    public String toString() {
        String msg = String.format("%n  @|yellow Pipeline '%s' updated at %s workspace|@", pipelineName, workspaceRef);
        if (draftVersionId != null) {
            msg += String.format("%n  @|yellow New draft version '%s' created. Use 'tw pipelines versions' to manage it.|@", draftVersionId);
        }
        return ansi(msg + String.format("%n"));
    }
}
