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

import io.seqera.tower.cli.commands.actions.CreateCmd;
import io.seqera.tower.cli.commands.actions.DeleteCmd;
import io.seqera.tower.cli.commands.actions.ListCmd;
import io.seqera.tower.cli.commands.actions.UpdateCmd;
import io.seqera.tower.cli.commands.actions.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "actions",
        description = "Manage actions.",
        subcommands = {
                ListCmd.class,
                ViewCmd.class,
                DeleteCmd.class,
                CreateCmd.class,
                UpdateCmd.class,
        }
)
public class ActionsCmd extends AbstractRootCmd {
}
