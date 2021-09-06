package io.seqera.tower.cli.commands.computeenv.create;

import io.seqera.tower.cli.commands.computeenv.platforms.AzBatchManualPlatform;
import io.seqera.tower.cli.commands.computeenv.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "azure-manual",
        description = "Create new Azure Batch compute environment using an existing environment"
)
public class CreateAzureManualCmd extends AbstractCreateCmd {

    @Mixin
    public AzBatchManualPlatform platform;

    @Override
    protected Platform getPlatform() {
        return platform;
    }
}
