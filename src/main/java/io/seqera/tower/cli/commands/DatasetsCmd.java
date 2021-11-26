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


import io.seqera.tower.cli.commands.datasets.CreateCmd;
import io.seqera.tower.cli.commands.datasets.DeleteCmd;
import io.seqera.tower.cli.commands.datasets.DownloadCmd;
import io.seqera.tower.cli.commands.datasets.ListCmd;
import io.seqera.tower.cli.commands.datasets.UpdateCmd;
import io.seqera.tower.cli.commands.datasets.UrlCmd;
import io.seqera.tower.cli.commands.datasets.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "datasets",
        description = "Manage datasets.",
        subcommands = {
                CreateCmd.class,
                DeleteCmd.class,
                DownloadCmd.class,
                ListCmd.class,
                ViewCmd.class,
                UpdateCmd.class,
                UrlCmd.class,
        }
)
public class DatasetsCmd extends AbstractRootCmd{
}
