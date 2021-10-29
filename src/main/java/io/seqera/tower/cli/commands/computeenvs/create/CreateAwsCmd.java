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
import picocli.CommandLine.Command;

@Command(
        name = "aws-batch",
        description = "Create new AWS Batch compute environment",
        subcommands = {
                CreateAwsForgeCmd.class,
                CreateAwsManualCmd.class,
        }
)
public class CreateAwsCmd extends AbstractComputeEnvCmd {
}

