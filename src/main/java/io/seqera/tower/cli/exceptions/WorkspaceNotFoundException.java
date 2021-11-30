/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
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
