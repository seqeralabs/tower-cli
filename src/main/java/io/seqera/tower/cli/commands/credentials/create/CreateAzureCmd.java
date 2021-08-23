package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.commands.credentials.providers.AzureProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "azure",
        description = "Create new Azure workspace credentials"
)
public class CreateAzureCmd extends AbstractCreateCmd {

    @Mixin
    AzureProvider provider;

    @Override
    protected AbstractProvider getProvider() {
        return provider;
    }
}
