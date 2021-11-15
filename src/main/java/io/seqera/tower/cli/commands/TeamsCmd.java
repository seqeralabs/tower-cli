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


import io.seqera.tower.cli.commands.teams.CreateCmd;
import io.seqera.tower.cli.commands.teams.DeleteCmd;
import io.seqera.tower.cli.commands.teams.ListCmd;
import io.seqera.tower.cli.commands.teams.MembersCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "teams",
        description = "Manage organization teams.",
        subcommands = {
                ListCmd.class,
                CreateCmd.class,
                DeleteCmd.class,
                MembersCmd.class,
        }
)
public class TeamsCmd extends AbstractRootCmd {
}
