package io.seqera.tower.cli.commands.computeenvs.create;

import io.seqera.tower.cli.commands.computeenvs.platforms.AltairPlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "altair",
        description = "Create new Altair PBS Pro compute environment"
)
public class CreateAltairCmd extends AbstractCreateCmd {

    @Mixin
    public AltairPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
