package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.members.CreateCmd;
import io.seqera.tower.cli.commands.members.DeleteCmd;
import io.seqera.tower.cli.commands.members.LeaveCmd;
import io.seqera.tower.cli.commands.members.ListCmd;
import io.seqera.tower.cli.commands.members.UpdateCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "members",
        description = "Manage organization's members",
        subcommands = {
                ListCmd.class,
                CreateCmd.class,
                DeleteCmd.class,
                UpdateCmd.class,
                LeaveCmd.class,
        }
)
public class MembersCmd extends AbstractRootCmd {
}
