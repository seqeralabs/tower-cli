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

public class RunSubmited extends Response {

    public final String workflowId;

    public final String workflowUrl;

    public final String workspaceRef;

    public RunSubmited(String workflowId, String workflowUrl, String workspaceRef) {
        this.workflowId = workflowId;
        this.workflowUrl = workflowUrl;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Workflow %s submitted at %s workspace.|@%n%n    @|bold %s|@%n", workflowId, workspaceRef, workflowUrl));
    }

}
