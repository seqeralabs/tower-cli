package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.commands.credentials.providers.BitbucketProvider;
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
    protected AbstractProvider getProvider() {
        return provider;
    }
}
