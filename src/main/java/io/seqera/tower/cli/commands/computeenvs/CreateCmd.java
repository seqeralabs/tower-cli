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
import io.seqera.tower.cli.commands.computeenvs.create.CreateAltairCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAwsCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateAzureCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateEksCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateGkeCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateGoogleCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateK8sCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateLsfCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateSlurmCmd;
import io.seqera.tower.cli.commands.computeenvs.create.CreateUgeCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "create",
        description = "Create new compute environment.",
        subcommands = {
                CreateK8sCmd.class,
                CreateAwsCmd.class,
                CreateEksCmd.class,
                CreateSlurmCmd.class,
                CreateLsfCmd.class,
                CreateUgeCmd.class,
                CreateAltairCmd.class,
                CreateGkeCmd.class,
                CreateGoogleCmd.class,
                CreateAzureCmd.class,
        }
)
public class CreateCmd extends AbstractComputeEnvCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
