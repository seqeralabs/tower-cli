package io.seqera.tower.cli.commands.computeenvs.create.aws;

import io.seqera.tower.cli.commands.computeenvs.create.AbstractCreateCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "forge",
        description = "Create new AWS Batch compute environment with automatic provisioning of compute resources"
)
public class CreateAwsForgeCmd extends AbstractCreateCmd {

    @Mixin
    public AwsBatchForgePlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
