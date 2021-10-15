package io.seqera.tower.cli.commands.actions;

import io.seqera.tower.cli.commands.actions.create.CreateGitHubCmd;
import io.seqera.tower.cli.commands.actions.create.CreateTowerCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "create",
        description = "Create a new Pipeline Action",
        subcommands = {
                CreateGitHubCmd.class,
                CreateTowerCmd.class
        }
)
public class CreateCmd extends AbstractActionsCmd {
}
