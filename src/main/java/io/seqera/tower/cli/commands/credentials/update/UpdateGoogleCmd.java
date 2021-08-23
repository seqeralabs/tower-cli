package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.GoogleProvider;
import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
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
    protected AbstractProvider getProvider() {
        return provider;
    }
}
