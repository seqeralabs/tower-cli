package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.actions.CreateCmd;
import io.seqera.tower.cli.commands.actions.DeleteCmd;
import io.seqera.tower.cli.commands.actions.ListCmd;
import io.seqera.tower.cli.commands.actions.UpdateCmd;
import io.seqera.tower.cli.commands.actions.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "actions",
        description = "Manage actions",
        subcommands = {
                ListCmd.class,
                ViewCmd.class,
                DeleteCmd.class,
                CreateCmd.class,
                UpdateCmd.class,
        }
)
public class ActionsCmd extends AbstractRootCmd {
}
