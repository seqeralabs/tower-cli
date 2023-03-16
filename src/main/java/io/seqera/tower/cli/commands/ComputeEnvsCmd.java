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

import io.seqera.tower.cli.commands.computeenvs.AddCmd;
import io.seqera.tower.cli.commands.computeenvs.DeleteCmd;
import io.seqera.tower.cli.commands.computeenvs.ExportCmd;
import io.seqera.tower.cli.commands.computeenvs.ImportCmd;
import io.seqera.tower.cli.commands.computeenvs.LabelsCmd;
import io.seqera.tower.cli.commands.computeenvs.ListCmd;
import io.seqera.tower.cli.commands.computeenvs.PrimaryCmd;
import io.seqera.tower.cli.commands.computeenvs.ViewCmd;
import picocli.CommandLine.Command;


@Command(
        name = "compute-envs",
        description = "Manage workspace compute environments.",
        subcommands = {
                AddCmd.class,
                DeleteCmd.class,
                ViewCmd.class,
                ListCmd.class,
                ExportCmd.class,
                ImportCmd.class,
                PrimaryCmd.class,
                LabelsCmd.class,
        }
)
public class ComputeEnvsCmd extends AbstractRootCmd {
}
