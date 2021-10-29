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

package io.seqera.tower.cli.commands.computeenvs.create;

import io.seqera.tower.cli.commands.computeenvs.AbstractComputeEnvCmd;
import io.seqera.tower.cli.commands.computeenvs.create.aws.CreateAwsForgeCmd;
import io.seqera.tower.cli.commands.computeenvs.create.aws.CreateAwsManualCmd;
import io.seqera.tower.cli.commands.computeenvs.create.azure.CreateAzureForgeCmd;
import io.seqera.tower.cli.commands.computeenvs.create.azure.CreateAzureManualCmd;
import picocli.CommandLine.Command;

@Command(
        name = "azure-batch",
        description = "Create new Azure Batch compute environments",
        subcommands = {
                CreateAzureForgeCmd.class,
                CreateAzureManualCmd.class,
        }
)
public class CreateAzureCmd extends AbstractComputeEnvCmd {
}

