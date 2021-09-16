package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.AwsProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.model.AwsSecurityKeys;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "aws",
        description = "Create new AWS workspace credentials"
)
public class CreateAwsCmd extends AbstractCreateCmd<AwsSecurityKeys> {

    @Mixin
    AwsProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
