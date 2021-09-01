package io.seqera.tower.cli.commands.computeenv;

import io.seqera.tower.cli.commands.computeenv.create.CreateAwsCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateAwsManualCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateJsonCmd;
import io.seqera.tower.cli.commands.computeenv.create.CreateK8sCmd;
import picocli.CommandLine.Command;

@Command(
        name = "create",
        aliases = "c",
        description = "Create new compute environment",
        subcommands = {
                CreateK8sCmd.class,
                CreateAwsCmd.class,
                CreateAwsManualCmd.class,
                CreateJsonCmd.class
        }
)
public class CreateCmd extends AbstractComputeEnvCmd {
}
