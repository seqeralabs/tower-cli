package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import io.seqera.tower.cli.commands.computeenv.platforms.SlurmPlatform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "altair",
        description = "Create new Altair PBS Pro compute environment"
)
public class CreateAltairCmd extends AbstractCreateCmd {

    @Mixin
    public SlurmPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
