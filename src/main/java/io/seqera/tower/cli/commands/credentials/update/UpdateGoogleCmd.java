package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.commands.credentials.providers.GoogleProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "google",
        description = "Update Google workspace credentials"
)
public class UpdateGoogleCmd extends AbstractUpdateCmd {

    @Mixin
    protected GoogleProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
