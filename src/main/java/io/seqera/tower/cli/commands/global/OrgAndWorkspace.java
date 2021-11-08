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

package io.seqera.tower.cli.commands.global;

import picocli.CommandLine;

public class OrgAndWorkspace {

    @CommandLine.Option(names = {"--workspace-name"}, description = "Workspace name (case insensitive)", defaultValue = "${TOWER_WORKSPACE_NAME}")
    public String workspace;

    @CommandLine.Option(names = {"--organization-name"}, description = "Organization name (case insensitive)", defaultValue = "${TOWER_ORG_NAME}")
    public String organization;
}
