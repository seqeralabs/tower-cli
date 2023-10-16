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

public class SecretAdded extends Response {

    public final String workspaceRef;
    public final Long id;
    public final String name;

    public SecretAdded(String workspaceRef, Long id, String name) {
        this.workspaceRef = workspaceRef;
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow New secret '%s' (%d) added at %s workspace|@%n", name, id, workspaceRef));
    }
}
