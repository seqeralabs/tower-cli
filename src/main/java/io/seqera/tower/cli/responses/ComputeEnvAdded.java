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

package io.seqera.tower.cli.responses;

public class ComputeEnvAdded extends Response {

    public final String platform;
    public final String name;
    public final String workspaceRef;

    public ComputeEnvAdded(String platform, String name, String workspaceRef) {
        this.platform = platform;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow New %S compute environment '%s' added at %s workspace|@%n", platform, name, workspaceRef));
    }
}
