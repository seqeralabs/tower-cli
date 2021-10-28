package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.pipelines.CreateCmd;
import io.seqera.tower.cli.commands.pipelines.DeleteCmd;
import io.seqera.tower.cli.commands.pipelines.ExportCmd;
import io.seqera.tower.cli.commands.pipelines.ImportCmd;
import io.seqera.tower.cli.commands.pipelines.ListCmd;
import io.seqera.tower.cli.commands.pipelines.UpdateCmd;
import io.seqera.tower.cli.commands.pipelines.ViewCmd;
import picocli.CommandLine.Command;


@Command(
        name = "pipelines",
        description = "Manage workspace pipelines launchpad",
        subcommands = {
                ListCmd.class,
                CreateCmd.class,
                DeleteCmd.class,
                ViewCmd.class,
                UpdateCmd.class,
                ExportCmd.class,
                ImportCmd.class,
        }
)
public class PipelinesCmd extends AbstractRootCmd {
}
