package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "aws",
        description = "Create new AWS Batch compute environment with automatic provisioning of compute resources"
)
public class CreateAwsCmd extends AbstractCreateCmd {

    @Mixin
    public AwsBatchForgePlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}