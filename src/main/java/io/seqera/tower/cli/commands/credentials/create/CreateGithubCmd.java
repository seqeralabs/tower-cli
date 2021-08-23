package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.commands.credentials.providers.GoogleProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "github",
        description = "Create new Github workspace credentials"
)
public class CreateGithubCmd extends AbstractCreateCmd {

    @Mixin
    GoogleProvider provider;

    @Override
    protected AbstractProvider getProvider() {
        return provider;
    }
}
