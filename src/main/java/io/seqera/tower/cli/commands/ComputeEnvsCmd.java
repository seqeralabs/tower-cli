package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.computeenvs.CreateCmd;
import io.seqera.tower.cli.commands.computeenvs.DeleteCmd;
import io.seqera.tower.cli.commands.computeenvs.ListCmd;
import io.seqera.tower.cli.commands.computeenvs.ViewCmd;
import picocli.CommandLine.Command;


@Command(
        name = "compute-envs",
        description = "Manage workspace compute environments",
        subcommands = {
                CreateCmd.class,
                DeleteCmd.class,
                ViewCmd.class,
                ListCmd.class
        }
)
public class ComputeEnvsCmd extends AbstractRootCmd {
}
