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

import io.seqera.tower.cli.commands.credentials.CreateCmd;
import io.seqera.tower.cli.commands.credentials.DeleteCmd;
import io.seqera.tower.cli.commands.credentials.ListCmd;
import io.seqera.tower.cli.commands.credentials.UpdateCmd;
import picocli.CommandLine.Command;


@Command(
        name = "credentials",
        description = "Manage workspace credentials.",
        subcommands = {
                CreateCmd.class,
                UpdateCmd.class,
                DeleteCmd.class,
                ListCmd.class
        }
)
public class CredentialsCmd extends AbstractRootCmd {
}
