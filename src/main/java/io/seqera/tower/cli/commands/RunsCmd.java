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

import io.seqera.tower.cli.commands.runs.CancelCmd;
import io.seqera.tower.cli.commands.runs.DeleteCmd;
import io.seqera.tower.cli.commands.runs.LabelsCmd;
import io.seqera.tower.cli.commands.runs.ListCmd;
import io.seqera.tower.cli.commands.runs.RelaunchCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "runs",
        description = "Manage workspace pipeline runs.",
        subcommands = {
                ViewCmd.class,
                ListCmd.class,
                RelaunchCmd.class,
                CancelCmd.class,
                DeleteCmd.class,
                LabelsCmd.class,
        }
)
public class RunsCmd extends AbstractRootCmd {
}
