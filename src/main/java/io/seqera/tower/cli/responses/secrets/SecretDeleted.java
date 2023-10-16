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

package io.seqera.tower.cli.responses.secrets;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.PipelineSecret;

public class SecretDeleted extends Response {

    public final PipelineSecret secret;
    public final String workspaceRef;

    public SecretDeleted(PipelineSecret secret, String workspaceRef) {
        this.secret = secret;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Secret '%s' deleted at %s workspace|@%n", secret.getName(), workspaceRef));
    }
}
