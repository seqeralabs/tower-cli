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

public class PipelinesUpdated extends Response {

    public final String workspaceRef;
    public final String pipelineName;

    public PipelinesUpdated(String workspaceRef, String pipelineName) {
        this.workspaceRef = workspaceRef;
        this.pipelineName = pipelineName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Pipeline '%s' updated at %s workspace|@%n", pipelineName, workspaceRef));
    }
}
