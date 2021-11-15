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

package io.seqera.tower.cli.commands.computeenvs;


import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.add.AddAltairCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddAwsCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddAzureCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddEksCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddGkeCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddGoogleCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddK8sCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddLsfCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddSlurmCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddUgeCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "add",
        description = "Add new compute environment",
        subcommands = {
                AddK8sCmd.class,
                AddAwsCmd.class,
                AddEksCmd.class,
                AddSlurmCmd.class,
                AddLsfCmd.class,
                AddUgeCmd.class,
                AddAltairCmd.class,
                AddGkeCmd.class,
                AddGoogleCmd.class,
                AddAzureCmd.class,
        }
)
public class AddCmd extends AbstractComputeEnvCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
