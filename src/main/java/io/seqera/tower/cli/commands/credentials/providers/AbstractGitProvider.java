package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine;

public abstract class AbstractGitProvider<T extends SecurityKeys> extends AbstractProvider<T> {

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
