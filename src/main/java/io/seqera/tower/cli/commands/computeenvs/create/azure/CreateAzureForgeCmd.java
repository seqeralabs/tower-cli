package io.seqera.tower.cli.commands.computeenvs.create.azure;

import io.seqera.tower.cli.commands.computeenvs.create.AbstractCreateCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.AzBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "forge",
        description = "Create new Azure Batch compute environment with automatic provisioning of compute resources"
)
public class CreateAzureForgeCmd extends AbstractCreateCmd {

    @Mixin
    public AzBatchForgePlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
