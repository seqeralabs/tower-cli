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

package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.workspaces.CreateCmd;
import io.seqera.tower.cli.commands.workspaces.DeleteCmd;
import io.seqera.tower.cli.commands.workspaces.LeaveCmd;
import io.seqera.tower.cli.commands.workspaces.ListCmd;
import io.seqera.tower.cli.commands.workspaces.UpdateCmd;
import io.seqera.tower.cli.commands.workspaces.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "workspaces",
        description = "Manage workspaces.",
        subcommands = {
                ListCmd.class,
                DeleteCmd.class,
                CreateCmd.class,
                UpdateCmd.class,
                ViewCmd.class,
                LeaveCmd.class,
        }
)
public class WorkspacesCmd extends AbstractRootCmd {
}
