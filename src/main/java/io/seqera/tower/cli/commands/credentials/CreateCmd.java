package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.BaseCmd;
import io.seqera.tower.cli.commands.CredentialsCmd;
import io.seqera.tower.cli.commands.credentials.create.AwsCmd;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(
        name = "create",
        description = "Create new workspace credentials",
        subcommands = {
                AwsCmd.class
        }
)
public class CreateCmd extends BaseCmd {

    @ParentCommand
    protected CredentialsCmd parent;

    @Override
    public Tower app() {
        return parent.app();
    }

}
