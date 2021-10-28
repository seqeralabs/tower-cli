package io.seqera.tower.cli.commands.computeenvs.create.azure;

import io.seqera.tower.cli.commands.computeenvs.create.AbstractCreateCmd;
import io.seqera.tower.cli.commands.computeenvs.platforms.AzBatchManualPlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.Platform;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "manual",
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
