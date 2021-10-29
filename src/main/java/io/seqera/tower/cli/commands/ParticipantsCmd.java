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

import io.seqera.tower.cli.commands.participants.AddCmd;
import io.seqera.tower.cli.commands.participants.ChangeCmd;
import io.seqera.tower.cli.commands.participants.DeleteCmd;
import io.seqera.tower.cli.commands.participants.LeaveCmd;
import io.seqera.tower.cli.commands.participants.ListCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "participants",
        description = "Manage workspace participants",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                ChangeCmd.class,
                DeleteCmd.class,
                LeaveCmd.class,
        }
)
public class ParticipantsCmd extends AbstractRootCmd {
}
