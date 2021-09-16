package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.BitbucketProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "bitbucket",
        description = "Create new Bitbucket workspace credentials"
)
public class CreateBitbucketCmd extends AbstractCreateCmd {

    @Mixin
    BitbucketProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
