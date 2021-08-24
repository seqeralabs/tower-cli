package io.seqera.tower.cli.commands.computeenv;

import io.seqera.tower.cli.commands.computeenv.create.CreateK8sCmd;
import picocli.CommandLine.Command;

@Command(
        name = "create",
        description = "Create new compute environment",
        subcommands = {
                CreateK8sCmd.class
        }
)
public class CreateCmd extends AbstractComputeEnvCmd {
}
