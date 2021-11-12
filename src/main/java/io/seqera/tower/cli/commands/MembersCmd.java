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

import io.seqera.tower.cli.commands.members.CreateCmd;
import io.seqera.tower.cli.commands.members.DeleteCmd;
import io.seqera.tower.cli.commands.members.LeaveCmd;
import io.seqera.tower.cli.commands.members.ListCmd;
import io.seqera.tower.cli.commands.members.UpdateCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "members",
        description = "Manage organization members",
        subcommands = {
                ListCmd.class,
                CreateCmd.class,
                DeleteCmd.class,
                UpdateCmd.class,
                LeaveCmd.class,
        }
)
public class MembersCmd extends AbstractRootCmd {
}
