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

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class WorkspacesMatchOptions {

    @ArgGroup(multiplicity = "1")
    public ByNameOrId match;

    public static class ByNameOrId {
        @ArgGroup(exclusive = false, heading = "%nMatch by workspace and organization name:%n")
        public MatchByName byName;

        @ArgGroup(heading = "%nMatch by workspace ID:%n")
        public MatchById byId;
    }


    public static class MatchByName {
        @Option(names = {"-n", "--name"}, description = "Workspace name", required = true)
        public String workspaceName;

        @Option(names = {"-o", "--org", "--organization"}, description = "Workspace organization name", required = true)
        public String organizationName;
    }

    public static class MatchById {
        @Option(names = {"-i", "--id"}, description = "Workspace id", required = true)
        public Long workspaceId;
    }
}
