package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.Tower;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command
public abstract class AbstractRootCmd extends AbstractCmd {

    @ParentCommand
    protected Tower app;

    public AbstractRootCmd() {
    }

    @Override
    public Tower app() {
        return app;
    }

}


