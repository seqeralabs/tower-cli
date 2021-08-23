package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.AwsProvider;
import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "aws",
        description = "Create new AWS workspace credentials"
)
public class CreateAwsCmd extends AbstractCreateCmd {

    @Mixin
    AwsProvider provider;

    @Override
    protected AbstractProvider getProvider() {
        return provider;
    }
}
