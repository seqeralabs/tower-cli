package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.computeenv.CreateCmd;
import io.seqera.tower.cli.commands.computeenv.DeleteCmd;
import io.seqera.tower.cli.commands.computeenv.ListCmd;
import io.seqera.tower.cli.commands.computeenv.ViewCmd;
import picocli.CommandLine.Command;


@Command(
        name = "compute-env",
        description = "Manage workspace compute environments",
        subcommands = {
                CreateCmd.class,
                DeleteCmd.class,
                ViewCmd.class,
                ListCmd.class
        }
)
public class ComputeEnvCmd extends AbstractRootCmd {
}
