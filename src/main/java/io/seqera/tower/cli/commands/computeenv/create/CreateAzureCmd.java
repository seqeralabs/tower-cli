package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.AzBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "azure",
        description = "Create new Azure Batch compute environment with automatic provisioning of compute resources"
)
public class CreateAzureCmd extends AbstractCreateCmd {

    @Mixin
    public AzBatchForgePlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
