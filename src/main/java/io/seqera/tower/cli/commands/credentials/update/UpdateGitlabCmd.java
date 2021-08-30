package io.seqera.tower.cli.commands.credentials.update;

import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import io.seqera.tower.cli.commands.credentials.providers.GitlabProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "gitlab",
        description = "Update Gitlab workspace credentials"
)
public class UpdateGitlabCmd extends AbstractUpdateCmd {

    @Mixin
    protected GitlabProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
