package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.commands.credentials.providers.SshProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "ssh",
        description = "Update SSH workspace credentials"
)
public class UpdateSshCmd extends AbstractUpdateCmd {

    @Mixin
    protected SshProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
