package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.commands.credentials.providers.GitlabProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "bitbucket",
        description = "Create new Bitbucket workspace credentials"
)
public class CreateBitbucketCmd extends AbstractCreateCmd {

    @Mixin
    GitlabProvider provider;

    @Override
    protected AbstractProvider getProvider() {
        return provider;
    }
}
