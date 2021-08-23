package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.GoogleProvider;
import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
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
    protected AbstractProvider getProvider() {
        return provider;
    }
}
