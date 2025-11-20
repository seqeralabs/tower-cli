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

package io.seqera.tower.cli.responses.labels;

import io.seqera.tower.cli.responses.Response;

import jakarta.annotation.Nullable;

public class DeleteLabelsResponse extends Response {

    public final Long labelId;
    @Nullable
    public final Long workspaceId;

    public DeleteLabelsResponse(final Long labelId, final Long workspaceId) {
        this.labelId = labelId;
        this.workspaceId = workspaceId;
    }

    @Override
    public String toString() {
        return ansi(String.format(
                "%n  @|yellow Label '%d' deleted"
                + ( workspaceId != null ? " at '%d' workspace" : "" )
                + " |@%n",
        labelId, workspaceId));
    }

}
