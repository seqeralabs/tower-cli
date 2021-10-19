package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.participants.AddCmd;
import io.seqera.tower.cli.commands.participants.ChangeCmd;
import io.seqera.tower.cli.commands.participants.DeleteCmd;
import io.seqera.tower.cli.commands.participants.LeaveCmd;
import io.seqera.tower.cli.commands.participants.ListCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "participants",
        description = "Manage workspace participants",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                ChangeCmd.class,
                DeleteCmd.class,
                LeaveCmd.class,
        }
)
public class ParticipantsCmd extends AbstractRootCmd {
}
