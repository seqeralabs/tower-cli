package io.seqera.tower.cli.commands.computeenv;

import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractCmd;
import io.seqera.tower.cli.commands.ComputeEnvCmd;
import io.seqera.tower.cli.commands.CredentialsCmd;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command
public abstract class AbstractComputeEnvCmd extends AbstractCmd {

    @ParentCommand
    protected ComputeEnvCmd parent;

    public AbstractComputeEnvCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
    }

}


