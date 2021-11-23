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

package io.seqera.tower.cli.commands.workspaces;

import picocli.CommandLine;

public class WorkspaceRefOptions {

    @CommandLine.ArgGroup
    public WorkspaceRef workspace;

    public static class WorkspaceRef {

        @CommandLine.Option(names = {"-i", "--id"}, description = "Workspace unique ID")
        public Long workspaceId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Workspace namespace in the form of organizationName/workspaceName")
        public String workspaceName;
    }
}
