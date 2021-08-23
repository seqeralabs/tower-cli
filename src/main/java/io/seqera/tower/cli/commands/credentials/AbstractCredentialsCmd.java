package io.seqera.tower.cli.commands.credentials;

import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractCmd;
import io.seqera.tower.cli.commands.CredentialsCmd;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command
public abstract class AbstractCredentialsCmd extends AbstractCmd {

    @ParentCommand
    protected CredentialsCmd parent;

    public AbstractCredentialsCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
    }

}


