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

public class InvalidWorkspaceParameterException extends TowerException {
    public InvalidWorkspaceParameterException(String workspaceRef) {
        super(String.format("Invalid workspace parameter '%s'. Expected format '<organization_name>/<workspace_name>' or '<workspace_numeric_identifier>'.", workspaceRef));
    }
}
