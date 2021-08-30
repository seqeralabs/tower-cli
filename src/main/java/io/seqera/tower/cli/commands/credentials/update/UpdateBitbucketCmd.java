package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.BitbucketProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "bitbucket",
        description = "Update Bitbucket workspace credentials"
)
public class UpdateBitbucketCmd extends AbstractUpdateCmd {

    @Mixin
    protected BitbucketProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
