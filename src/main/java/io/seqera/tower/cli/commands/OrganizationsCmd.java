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

import io.seqera.tower.cli.commands.organizations.AddCmd;
import io.seqera.tower.cli.commands.organizations.DeleteCmd;
import io.seqera.tower.cli.commands.organizations.ListCmd;
import io.seqera.tower.cli.commands.organizations.UpdateCmd;
import io.seqera.tower.cli.commands.organizations.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "organizations",
        description = "Manage organizations.",
        subcommands = {
                ListCmd.class,
                DeleteCmd.class,
                AddCmd.class,
                UpdateCmd.class,
                ViewCmd.class,
        }
)
public class OrganizationsCmd extends AbstractRootCmd {
}
