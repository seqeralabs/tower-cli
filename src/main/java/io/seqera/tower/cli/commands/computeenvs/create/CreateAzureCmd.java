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

