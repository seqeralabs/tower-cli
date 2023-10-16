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

package io.seqera.tower.cli.exceptions;

public class WorkspaceNotFoundException extends TowerException {

    public WorkspaceNotFoundException(Long workspaceId) {
        super(String.format("Workspace '%d' not found", workspaceId));
    }

    public WorkspaceNotFoundException(String workspaceName) {
        super(String.format("Workspace '%s' not found", workspaceName));
    }

    public WorkspaceNotFoundException(String workspaceName, String orgName) {
        super(String.format("Workspace '%s' at organization '%s' not found", workspaceName, orgName));
    }

    public WorkspaceNotFoundException(Long workspaceId, Long orgId) {
        super(String.format("Workspace '%s' at organization '%s' not found", workspaceId, orgId));
    }
}
