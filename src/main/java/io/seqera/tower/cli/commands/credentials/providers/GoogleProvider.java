package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GoogleProvider extends AbstractProvider {

    @Option(names = {"-k", "--key"}, description = "JSON file with the service account key", required = true)
    public Path serviceAccountKey;

    public GoogleProvider() {
        super(ProviderEnum.GOOGLE);
    }

    @Override
    public SecurityKeys securityKeys() throws IOException {
        SecurityKeys result = new SecurityKeys();
        result.data(Files.readString(serviceAccountKey));
        return result;
    }
}
