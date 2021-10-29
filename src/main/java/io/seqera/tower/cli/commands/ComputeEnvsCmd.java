package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.computeenvs.CreateCmd;
import io.seqera.tower.cli.commands.computeenvs.DeleteCmd;
import io.seqera.tower.cli.commands.computeenvs.ExportCmd;
import io.seqera.tower.cli.commands.computeenvs.ImportCmd;
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
                ListCmd.class,
                ExportCmd.class,
                ImportCmd.class,
        }
)
public class ComputeEnvsCmd extends AbstractRootCmd {
}
