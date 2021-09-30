package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.ComputeEnvsCmd;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command
public abstract class AbstractComputeEnvCmd extends AbstractApiCmd {

    @ParentCommand
    protected ComputeEnvsCmd parent;

    public AbstractComputeEnvCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
    }

}


