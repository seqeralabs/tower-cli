package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;

import java.io.IOException;

public abstract class AbstractProvider<T extends SecurityKeys> implements CredentialsProvider {
    private ProviderEnum type;

    public AbstractProvider(ProviderEnum type) {
        this.type = type;
    }

    public String baseUrl() {
        return null;
    }

    public ProviderEnum type() {
        return type;
    }

    public abstract T securityKeys() throws IOException, ApiException;
}
