package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.Tower;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.commands.ComputeEnvsCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

import java.io.IOException;

@Command
public abstract class AbstractComputeEnvCmd extends AbstractApiCmd {

    @ParentCommand
    protected ComputeEnvsCmd parent;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec commandSpec;

    public AbstractComputeEnvCmd() {
    }

    @Override
    public Tower app() {
        return parent.app();
    }

}


