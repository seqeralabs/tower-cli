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
import picocli.CommandLine.Option;

public class WorkspaceOptions {

    @Option(names = {"-o", "--org", "--organization"}, description = "The workspace organization name", required = true)
    public String organizationName;

    @Option(names = {"-n", "--name"}, description = "The workspace short name. Only alphanumeric, dash and underscore characters are allowed", required = true)
    public String workspaceName;

    @CommandLine.Option(names = {"-f", "--full-name"}, description = "The workspace full name", required = true)
    public String workspaceFullName;

    @Option(names = {"-d", "--description"}, description = "The workspace description")
    public String description;


}
