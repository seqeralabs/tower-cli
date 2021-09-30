package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.workspaces.CreateCmd;
import io.seqera.tower.cli.commands.workspaces.DeleteCmd;
import io.seqera.tower.cli.commands.workspaces.ListCmd;
import io.seqera.tower.cli.commands.workspaces.UpdateCmd;
import io.seqera.tower.cli.commands.workspaces.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "workspaces",
        description = "Manage workspaces",
        subcommands = {
                ListCmd.class,
                DeleteCmd.class,
                CreateCmd.class,
                UpdateCmd.class,
                ViewCmd.class,
        }
)
public class WorkspacesCmd extends AbstractRootCmd {
}
