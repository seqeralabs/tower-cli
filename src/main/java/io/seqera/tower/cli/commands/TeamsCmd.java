package io.seqera.tower.cli.commands;


import io.seqera.tower.cli.commands.teams.CreateCmd;
import io.seqera.tower.cli.commands.teams.DeleteCmd;
import io.seqera.tower.cli.commands.teams.ListCmd;
import io.seqera.tower.cli.commands.teams.MembersCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "teams",
        description = "Manage organization's teams",
        subcommands = {
                ListCmd.class,
                CreateCmd.class,
                DeleteCmd.class,
                MembersCmd.class,
        }
)
public class TeamsCmd extends AbstractRootCmd {
}
