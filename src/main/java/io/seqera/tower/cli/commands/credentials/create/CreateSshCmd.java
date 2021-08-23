package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.AbstractProvider;
import io.seqera.tower.cli.commands.credentials.providers.SshProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "ssh",
        description = "Create new SSH workspace credentials"
)
public class CreateSshCmd extends AbstractCreateCmd {

    @Mixin
    SshProvider provider;

    @Override
    protected AbstractProvider getProvider() {
        return provider;
    }
}
