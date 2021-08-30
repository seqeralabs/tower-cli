package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.GoogleProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
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
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
