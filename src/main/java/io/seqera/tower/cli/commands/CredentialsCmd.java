package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.credentials.CreateCmd;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;


@Command(
        name = "credentials",
        aliases = {"creds"},
        description = "Manage workspace credentials",
        subcommands = {
                CreateCmd.class
        }
)
public class CredentialsCmd extends BaseCmd {

    @ParentCommand
    protected Tower app;

    @Override
    public Tower app() {
        return app;
    }
}
