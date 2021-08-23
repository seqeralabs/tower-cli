package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.commands.credentials.providers.GithubProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "github",
        description = "Update Github workspace credentials"
)
public class UpdateGithubCmd extends AbstractUpdateCmd {

    @Mixin
    protected GithubProvider provider;

    @Override
    protected AbstractProvider getProvider() {
        return provider;
    }
}
