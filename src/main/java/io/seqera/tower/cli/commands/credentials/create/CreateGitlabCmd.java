package io.seqera.tower.cli.commands.credentials.create;

import io.seqera.tower.cli.commands.credentials.providers.GitlabProvider;
import io.seqera.tower.cli.commands.credentials.providers.CredentialsProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        name = "gitlab",
        description = "Create new Gitlab workspace credentials"
)
public class CreateGitlabCmd extends AbstractCreateCmd {

    @Mixin
    GitlabProvider provider;

    @Override
    protected CredentialsProvider getProvider() {
        return provider;
    }
}
