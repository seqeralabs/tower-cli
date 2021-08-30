package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.GoogleSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GoogleProvider extends AbstractProvider<GoogleSecurityKeys> {

    @Option(names = {"-k", "--key"}, description = "JSON file with the service account key", required = true)
    public Path serviceAccountKey;

    public GoogleProvider() {
        super(ProviderEnum.GOOGLE);
    }

    @Override
    public GoogleSecurityKeys securityKeys() throws IOException {
        return new GoogleSecurityKeys()
                .data(Files.readString(serviceAccountKey));
    }
}
