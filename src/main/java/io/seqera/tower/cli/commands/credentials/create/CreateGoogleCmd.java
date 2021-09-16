package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.commands.credentials.providers.GoogleProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "google",
        description = "Create new Google workspace credentials"
)
public class CreateGoogleCmd extends AbstractCreateCmd {

    @Mixin
    GoogleProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
