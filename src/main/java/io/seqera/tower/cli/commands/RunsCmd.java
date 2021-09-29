package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.runs.CancelCmd;
import io.seqera.tower.cli.commands.runs.DeleteCmd;
import io.seqera.tower.cli.commands.runs.RelaunchCmd;
import io.seqera.tower.cli.commands.runs.ListCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "runs",
        description = "Manage workspace pipeline's runs",
        subcommands = {
                ViewCmd.class,
                ListCmd.class,
                RelaunchCmd.class,
                CancelCmd.class,
                DeleteCmd.class
        }
)
public class RunsCmd extends AbstractRootCmd {
}
