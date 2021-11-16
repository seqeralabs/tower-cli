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

import io.seqera.tower.cli.commands.pipelines.AddCmd;
import io.seqera.tower.cli.commands.pipelines.DeleteCmd;
import io.seqera.tower.cli.commands.pipelines.ExportCmd;
import io.seqera.tower.cli.commands.pipelines.ImportCmd;
import io.seqera.tower.cli.commands.pipelines.ListCmd;
import io.seqera.tower.cli.commands.pipelines.UpdateCmd;
import io.seqera.tower.cli.commands.pipelines.ViewCmd;
import picocli.CommandLine.Command;


@Command(
        name = "pipelines",
        description = "Manage workspace pipeline launchpad.",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                DeleteCmd.class,
                ViewCmd.class,
                UpdateCmd.class,
                ExportCmd.class,
                ImportCmd.class,
        }
)
public class PipelinesCmd extends AbstractRootCmd {
}
