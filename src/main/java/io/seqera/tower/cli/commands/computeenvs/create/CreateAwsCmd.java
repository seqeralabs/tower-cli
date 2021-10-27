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

