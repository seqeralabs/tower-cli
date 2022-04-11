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

public class ComputeEnvNotFoundException extends TowerException {
    public ComputeEnvNotFoundException(String name, String workspaceRef) {
        super(String.format("Compute environment '%s' not found at %s workspace", name, workspaceRef));
    }

    public ComputeEnvNotFoundException(String name, Long workspaceId) {
        super(String.format("Compute environment '%s' not found at %s workspace", name, workspaceId != null ? workspaceId.toString() : "user"));
    }
}
