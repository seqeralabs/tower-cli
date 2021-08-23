package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.credentials.CreateCmd;
import io.seqera.tower.cli.commands.credentials.DeleteCmd;
import io.seqera.tower.cli.commands.credentials.ListCmd;
import io.seqera.tower.cli.commands.credentials.UpdateCmd;
import picocli.CommandLine.Command;


@Command(
        name = "credentials",
        aliases = {"creds"},
        description = "Manage workspace credentials",
        subcommands = {
                CreateCmd.class,
                UpdateCmd.class,
                DeleteCmd.class,
                ListCmd.class
        }
)
public class CredentialsCmd extends AbstractRootCmd {
}
