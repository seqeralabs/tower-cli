package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.AzureProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "azure",
        description = "Update Azure workspace credentials"
)
public class UpdateAzureCmd extends AbstractUpdateCmd {

    @Mixin
    protected AzureProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
