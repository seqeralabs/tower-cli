package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.ApiException;
import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;

import java.io.IOException;

public interface CredentialsProvider {
    String baseUrl();

    ProviderEnum type();

    SecurityKeys securityKeys() throws IOException, ApiException;
}
