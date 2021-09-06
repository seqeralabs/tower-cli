package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.LsfPlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
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
