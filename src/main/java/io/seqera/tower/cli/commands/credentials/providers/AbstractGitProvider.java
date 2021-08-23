package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import picocli.CommandLine;

public abstract class AbstractGitProvider extends AbstractProvider {

    @CommandLine.Option(names = {"--base-url"}, description = "Repository base URL")
    public String baseUrl;

    public AbstractGitProvider(ProviderEnum type) {
        super(type);
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

}
