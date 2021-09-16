package io.seqera.tower.cli.commands.computeenvs.create;

import io.seqera.tower.cli.commands.computeenvs.platforms.LsfPlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "lsf",
        description = "Create new IBM LSF compute environment"
)
public class CreateLsfCmd extends AbstractCreateCmd {

    @Mixin
    public LsfPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
