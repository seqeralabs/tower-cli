package io.seqera.tower.cli.commands.computeenvs;

import java.util.Objects;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.cli.exceptions.ComputeEnvNotFoundException;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ListComputeEnvsResponse;
import io.seqera.tower.model.ListComputeEnvsResponseEntry;
import picocli.CommandLine.Command;

@Command
public abstract class AbstractComputeEnvCmd extends AbstractApiCmd {

    public AbstractComputeEnvCmd() {
    }
}


