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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.commands.AbstractRootCmd;
import io.seqera.tower.cli.commands.computeenvs.*;
import picocli.CommandLine.Command;


@Command(
        name = "labels",
        description = "Manage labels.",
        subcommands = {
                AddLabelsCmd.class,
                ListLabelsCmd.class,
                UpdateLabelsCmd.class,
                DeleteLabelsCmd.class
        }
)
public class LabelsCmd extends AbstractRootCmd {


}
